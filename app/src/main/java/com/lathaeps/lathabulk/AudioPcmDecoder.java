package com.lathaeps.lathabulk;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;

/** Decodes a shared WhatsApp audio file into mono, 16 kHz, PCM-16 speech audio. */
final class AudioPcmDecoder {
    static File decode(Context context, Uri source) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec decoder = null;
        try {
            extractor.setDataSource(context, source, Collections.emptyMap());
            int track = -1;
            MediaFormat inputFormat = null;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat candidate = extractor.getTrackFormat(i);
                String mime = candidate.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    track = i;
                    inputFormat = candidate;
                    break;
                }
            }
            if (track < 0 || inputFormat == null) throw new Exception("Audio track नहीं मिला");
            extractor.selectTrack(track);
            String mime = inputFormat.getString(MediaFormat.KEY_MIME);
            if (mime == null) throw new Exception("Audio format unknown");

            int sampleRate = inputFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)
                    ? inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 48000;
            int channels = inputFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)
                    ? inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 1;
            int pcmEncoding = AudioFormat.ENCODING_PCM_16BIT;

            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(inputFormat, null, null, 0);
            decoder.start();

            ByteArrayOutputStream decoded = new ByteArrayOutputStream();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean inputDone = false, outputDone = false;
            long startedAt = System.currentTimeMillis();

            while (!outputDone) {
                if (System.currentTimeMillis() - startedAt > 120000L)
                    throw new Exception("Voice note decode timeout");

                if (!inputDone) {
                    int inputIndex = decoder.dequeueInputBuffer(10000);
                    if (inputIndex >= 0) {
                        ByteBuffer input = decoder.getInputBuffer(inputIndex);
                        if (input == null) continue;
                        input.clear();
                        int size = extractor.readSampleData(input, 0);
                        if (size < 0) {
                            decoder.queueInputBuffer(inputIndex, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            decoder.queueInputBuffer(inputIndex, 0, size,
                                    extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outputIndex = decoder.dequeueOutputBuffer(info, 10000);
                if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat out = decoder.getOutputFormat();
                    if (out.containsKey(MediaFormat.KEY_SAMPLE_RATE))
                        sampleRate = out.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    if (out.containsKey(MediaFormat.KEY_CHANNEL_COUNT))
                        channels = out.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    if (out.containsKey(MediaFormat.KEY_PCM_ENCODING))
                        pcmEncoding = out.getInteger(MediaFormat.KEY_PCM_ENCODING);
                } else if (outputIndex >= 0) {
                    ByteBuffer output = decoder.getOutputBuffer(outputIndex);
                    if (output != null && info.size > 0) {
                        output.position(info.offset);
                        output.limit(info.offset + info.size);
                        byte[] chunk = new byte[info.size];
                        output.get(chunk);
                        decoded.write(chunk);
                        if (decoded.size() > 120 * 1024 * 1024)
                            throw new Exception("Voice note बहुत लंबा है");
                    }
                    outputDone = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                    decoder.releaseOutputBuffer(outputIndex, false);
                }
            }

            byte[] mono16k = toMono16k(decoded.toByteArray(), sampleRate,
                    Math.max(1, channels), pcmEncoding);
            if (mono16k.length < 3200) throw new Exception("Voice note खाली है");
            File out = new File(context.getCacheDir(),
                    "customer_voice_" + System.currentTimeMillis() + ".pcm");
            try (FileOutputStream stream = new FileOutputStream(out)) {
                stream.write(mono16k);
            }
            return out;
        } finally {
            try { extractor.release(); } catch (Exception ignored) {}
            if (decoder != null) {
                try { decoder.stop(); } catch (Exception ignored) {}
                try { decoder.release(); } catch (Exception ignored) {}
            }
        }
    }

    private static byte[] toMono16k(byte[] source, int sampleRate, int channels,
                                    int encoding) throws Exception {
        if (sampleRate <= 0) sampleRate = 48000;
        int bytesPerSample = encoding == AudioFormat.ENCODING_PCM_FLOAT ? 4 : 2;
        if (encoding != AudioFormat.ENCODING_PCM_FLOAT
                && encoding != AudioFormat.ENCODING_PCM_16BIT)
            throw new Exception("Unsupported decoded audio");
        int frames = source.length / Math.max(1, channels * bytesPerSample);
        if (frames <= 0) return new byte[0];
        int outputFrames = Math.max(1, (int) ((long) frames * 16000L / sampleRate));
        ByteBuffer input = ByteBuffer.wrap(source).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer output = ByteBuffer.allocate(outputFrames * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < outputFrames; i++) {
            int sourceFrame = Math.min(frames - 1, (int) ((long) i * sampleRate / 16000L));
            float total = 0f;
            for (int channel = 0; channel < channels; channel++) {
                int offset = (sourceFrame * channels + channel) * bytesPerSample;
                if (encoding == AudioFormat.ENCODING_PCM_FLOAT)
                    total += input.getFloat(offset);
                else
                    total += input.getShort(offset) / 32768f;
            }
            float mono = Math.max(-1f, Math.min(1f, total / channels));
            output.putShort((short) Math.round(mono * 32767f));
        }
        return output.array();
    }

    private AudioPcmDecoder() {}
}

# LathaBulk v3.11 – Master Ledger PDF to Excel

## Update flow
1. Open **BUSINESS FILES**.
2. Main screen par **MASTER LEDGER PDF → EXCEL (.xlsx)** tap karke Tally PDF select karein and Excel save karein.
3. Customer matching ke liye **BUSINESS FILES → IMPORT CUSTOMER EXCEL (.xlsx)** select karein.
4. Excel first-sheet columns:
   - Phone Number
   - Customer Name
   - Ledger File Name
5. Save ledger keyword, then keep Notification Access and Accessibility enabled.

## Important
PDF-to-Excel export text-based Tally PDFs ke page/line data ko editable Excel rows me save karta hai. Scanned/image-only PDF ke liye OCR required hoga. Customer-wise private sending ke liye separate ledger PDFs aur phone mapping required hai.

## Build APK
Upload the extracted project to GitHub. GitHub Actions workflow is included under `.github/workflows/build-apk.yml`.

# LathaBulk v3.12 – Master PDF to Excel + Phone-wise Ledger

## Update flow
1. Open **BUSINESS FILES**.
2. Tap **1. UPDATE MASTER LEDGER PDF** if you want to keep the combined master PDF as an app reference.
3. Tap **2. MASTER LEDGER EXCEL UPDATE (.xlsx)** and select the customer mapping workbook.
4. Excel first-sheet columns:
   - Phone Number
   - Customer Name
   - Ledger File Name
5. Tap **3. IMPORT CUSTOMER LEDGER PDFs (MULTIPLE)** and select all separately exported customer ledger PDFs.
6. PDF filenames are matched first with `Ledger File Name`, then phone number, then customer name.
7. Tap **Save & Turn ON**, then keep Notification Access and Accessibility enabled.

## Master Ledger PDF → Excel
1. Select **UPDATE MASTER LEDGER PDF**.
2. Tap **AUTO CONVERT MASTER PDF TO EXCEL**.
3. Choose where to save `Latha_Master_Ledger_Converted.xlsx`.
4. The app extracts page-wise Phone Number, Customer Name, suggested Ledger File Name, Closing Balance, PDF Page and full ledger text.

Text-based Tally PDFs are supported. If the PDF is a scanned photo without selectable text, export it again from Tally as a normal text PDF.

## Important
This build never sends the combined master PDF to a matched customer. Incoming WhatsApp phone number/name is matched with Excel, and only that customer's mapped PDF is sent. It does not split a combined Tally master PDF automatically; export separate ledger PDFs from Tally first.

Auto-reply image/PDF sending now waits for the WhatsApp attachment preview before Accessibility presses Send, fixing the previous preview-window-stops issue.

## Build APK
Upload the extracted project to GitHub. GitHub Actions workflow is included under `.github/workflows/build-apk.yml`.

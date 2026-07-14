# LathaBulk v3.10 – Master Ledger PDF + Excel Update

## Update flow
1. Open **BUSINESS FILES**.
2. Tap **UPDATE MASTER LEDGER PDF** and select the latest PDF.
3. Tap **UPDATE CUSTOMER EXCEL (.xlsx)** and select the customer mapping workbook.
4. Excel first-sheet columns:
   - Phone Number
   - Customer Name
   - Ledger File Name
5. Save ledger keyword, then keep Notification Access and Accessibility enabled.

## Important
This build stores the selected master PDF safely inside the app and updates customer phone/name mapping from Excel. It does not split a combined Tally master PDF into separate customer PDFs automatically. For private customer-wise ledger sending, export separate ledger PDFs and map their filenames.

## Build APK
Upload the extracted project to GitHub. GitHub Actions workflow is included under `.github/workflows/build-apk.yml`.

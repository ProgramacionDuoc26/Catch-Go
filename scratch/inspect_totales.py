import openpyxl

wb = openpyxl.load_workbook(r"c:\Users\xkait\Desktop\Taller\Catch-Go\Documentacion\Unidad #3\3.1.3 Planilla Casos de Prueba.xlsx")
sheet = wb['Totales']
print("Sheet title:", sheet.title)

for r_idx, row in enumerate(sheet.iter_rows(values_only=True), start=1):
    if any(row):
        print(f"Row {r_idx}:", row)

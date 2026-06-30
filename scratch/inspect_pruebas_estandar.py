import openpyxl

wb = openpyxl.load_workbook(r"c:\Users\xkait\Desktop\Taller\Catch-Go\Documentacion\Unidad #3\3.1.3 Planilla Casos de Prueba.xlsx")
sheet = wb['Pruebas Estandar ']

print("--- Printing remaining rows ---")
for r_idx in range(40, 95):
    row_vals = [sheet.cell(row=r_idx, column=c).value for c in range(1, 6)]
    if any(row_vals):
        print(f"Row {r_idx}:", row_vals)

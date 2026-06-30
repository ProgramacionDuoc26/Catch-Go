import openpyxl

wb = openpyxl.load_workbook(r"c:\Users\xkait\Desktop\Taller\Catch-Go\Documentacion\Unidad #3\3.1.3 Planilla Casos de Prueba.xlsx")
sheet = wb['Casos de Prueba']

# Imprimir las últimas filas (de la 65 a la 75)
for r_idx in range(63, 75):
    row_vals = [cell.value for cell in sheet[r_idx]]
    print(f"Row {r_idx}:", row_vals[:12])

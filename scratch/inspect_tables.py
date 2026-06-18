import docx

doc = docx.Document(r"c:\Users\xkait\Desktop\Taller\Catch-Go\Documentacion\Unidad #3\3.1.2 Plan Pruebas Funcionales.docx")
table = doc.tables[4]
print("Table 4 total rows:", len(table.rows))

# Imprimir las últimas 30 filas de la tabla 4
for r_idx, r in enumerate(table.rows[-30:], start=len(table.rows)-30):
    print(f"Row {r_idx}:", [cell.text.strip().replace('\n', ' ')[:50] for cell in r.cells])

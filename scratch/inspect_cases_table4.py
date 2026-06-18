import docx
import re

doc = docx.Document(r"c:\Users\xkait\Desktop\Taller\Catch-Go\Documentacion\Unidad #3\3.1.2 Plan Pruebas Funcionales.docx")
table = doc.tables[4]

found_cases = []
for r_idx, r in enumerate(table.rows):
    # En la primera fila de cada caso, la celda 1 suele tener el ID del caso (ej: CP-01)
    cell_text = r.cells[1].text.strip()
    if re.match(r'^CP-\d+$', cell_text):
        found_cases.append((r_idx, cell_text, r.cells[0].text.strip()))

print(f"Total cases in Table 4: {len(found_cases)}")
for idx, cp_id, name in found_cases:
    print(f"Row {idx}: {cp_id} - {name}")

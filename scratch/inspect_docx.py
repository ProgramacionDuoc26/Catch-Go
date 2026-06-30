import docx

doc = docx.Document(r"c:\Users\xkait\Desktop\Taller\Catch-Go\Documentacion\Unidad #3\3.1.2 Plan Pruebas Funcionales.docx")

# Imprimir todos los párrafos que tengan formato de título o texto en mayúsculas
for i, p in enumerate(doc.paragraphs):
    if p.text and (p.style.name.startswith('Heading') or len(p.text) < 50):
        print(f"P {i} ({p.style.name}): {p.text}")

print("\n--- Tables ---")
for t_idx, table in enumerate(doc.tables):
    # Imprimir la primera celda de la primera fila de cada tabla
    print(f"Table {t_idx}: shape={len(table.rows)}x{len(table.columns)}, first cell={table.cell(0,0).text[:50]}")

#!/usr/bin/env python3
"""
Convierte los documentos Markdown de docs/ a PDF reales usando fpdf2.
No es un renderer Markdown completo: soporta lo que realmente usan nuestros
documentos (encabezados #/##/###, tablas con pipes, listas con -, negritas
con **, bloques de código, y párrafos). Los bloques ```mermaid se muestran
como texto monoespaciado con una nota (el diagrama interactivo vive en
docs/BASE_DE_DATOS.md).
"""
import os
import re
from fpdf import FPDF

DOCS = os.path.join(os.path.dirname(__file__), "..", "docs")
OUT = os.path.join(DOCS, "pdf")
os.makedirs(OUT, exist_ok=True)

NAVY = (27, 42, 56)
ORANGE = (255, 107, 26)
GRAY = (90, 90, 90)
LIGHT = (246, 243, 236)


class ConstructopolisPDF(FPDF):
    def __init__(self, title):
        super().__init__(format="A4")
        self.doc_title = title
        self.set_auto_page_break(auto=True, margin=20)
        self.set_margins(18, 18, 18)

    def header(self):
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(*ORANGE)
        self.cell(0, 8, "CONSTRUCTÓPOLIS", align="L")
        self.set_text_color(*GRAY)
        self.cell(0, 8, self.doc_title, align="R", new_x="LMARGIN", new_y="NEXT")
        self.set_draw_color(*ORANGE)
        self.set_line_width(0.6)
        self.line(18, 16, 192, 16)
        self.ln(4)

    def footer(self):
        self.set_y(-15)
        self.set_font("Helvetica", "", 8)
        self.set_text_color(*GRAY)
        self.cell(0, 10, f"Página {self.page_no()}", align="C")


def sanitize(text):
    """Reemplaza caracteres tipográficos fuera de Latin-1 (fuentes core) por equivalentes seguros."""
    replacements = {
        "\u2014": " - ", "\u2013": "-", "\u2026": "...",
        "\u2018": "'", "\u2019": "'", "\u201c": '"', "\u201d": '"',
        "\u2192": "->", "\u2022": "-", "\u00a0": " ",
    }
    for src, dst in replacements.items():
        text = text.replace(src, dst)
    return text.encode("latin-1", "replace").decode("latin-1")


def clean_bold(text):
    """Convierte **negrita** en segmentos; devuelve lista de (texto, is_bold)."""
    text = sanitize(text)
    parts = []
    tokens = re.split(r"(\*\*.*?\*\*)", text)
    for tok in tokens:
        if tok.startswith("**") and tok.endswith("**") and len(tok) > 4:
            parts.append((tok[2:-2], True))
        elif tok:
            parts.append((tok, False))
    return parts


def write_rich_line(pdf, text, size=10.5):
    pdf.set_font("Helvetica", "", size)
    pdf.set_text_color(35, 35, 35)
    x_start = pdf.get_x()
    for chunk, bold in clean_bold(text):
        pdf.set_font("Helvetica", "B" if bold else "", size)
        pdf.write(6, chunk)
    pdf.ln(7)


def render_table(pdf, rows):
    ncols = len(rows[0])
    page_width = pdf.w - pdf.l_margin - pdf.r_margin
    col_width = page_width / ncols
    pdf.set_font("Helvetica", "B", 9.5)
    pdf.set_fill_color(*NAVY)
    pdf.set_text_color(255, 255, 255)
    for cell in rows[0]:
        pdf.cell(col_width, 8, sanitize(cell.strip()), border=1, fill=True)
    pdf.ln(8)
    pdf.set_font("Helvetica", "", 9)
    pdf.set_text_color(35, 35, 35)
    fill = False
    for row in rows[1:]:
        pdf.set_fill_color(*LIGHT) if fill else pdf.set_fill_color(255, 255, 255)
        for cell in row:
            pdf.cell(col_width, 7.5, sanitize(cell.strip()[:60]), border=1, fill=True)
        pdf.ln(7.5)
        fill = not fill
    pdf.ln(3)


def render_markdown(pdf, md_text):
    lines = md_text.split("\n")
    i = 0
    in_code = False
    code_buffer = []
    while i < len(lines):
        line = lines[i]

        if line.strip().startswith("```"):
            if not in_code:
                in_code = True
                code_buffer = []
            else:
                in_code = False
                pdf.set_font("Courier", "", 8)
                pdf.set_fill_color(*LIGHT)
                pdf.set_text_color(40, 40, 40)
                block = sanitize("\n".join(code_buffer))
                pdf.multi_cell(0, 4.6, block, fill=True)
                pdf.ln(2)
            i += 1
            continue
        if in_code:
            code_buffer.append(line)
            i += 1
            continue

        if not line.strip():
            pdf.ln(2)
            i += 1
            continue

        if line.startswith("# "):
            pdf.set_font("Helvetica", "B", 19)
            pdf.set_text_color(*NAVY)
            pdf.multi_cell(0, 10, sanitize(line[2:].strip()))
            pdf.ln(1)
            i += 1
            continue
        if line.startswith("## "):
            pdf.ln(2)
            pdf.set_font("Helvetica", "B", 14.5)
            pdf.set_text_color(*ORANGE)
            pdf.multi_cell(0, 8.5, sanitize(line[3:].strip()))
            pdf.ln(1)
            i += 1
            continue
        if line.startswith("### "):
            pdf.ln(1)
            pdf.set_font("Helvetica", "B", 12)
            pdf.set_text_color(*NAVY)
            pdf.multi_cell(0, 7.5, sanitize(line[4:].strip()))
            i += 1
            continue

        if line.strip().startswith("|"):
            table_lines = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                table_lines.append(lines[i])
                i += 1
            rows = []
            for tl in table_lines:
                cells = [c for c in tl.strip().strip("|").split("|")]
                if all(re.match(r"^\s*-+\s*$", c) for c in cells):
                    continue
                rows.append(cells)
            if rows:
                render_table(pdf, rows)
            continue

        if re.match(r"^\s*[-*]\s+", line):
            text = re.sub(r"^\s*[-*]\s+", "", line)
            pdf.set_x(pdf.l_margin + 4)
            pdf.set_font("Helvetica", "", 10.5)
            pdf.set_text_color(255, 107, 26)
            pdf.cell(4, 6, chr(149))
            pdf.set_text_color(35, 35, 35)
            for chunk, bold in clean_bold(text):
                pdf.set_font("Helvetica", "B" if bold else "", 10.5)
                pdf.write(6, chunk)
            pdf.ln(6.5)
            i += 1
            continue

        if re.match(r"^\s*\d+\.\s+", line):
            text = re.sub(r"^\s*\d+\.\s+", "", line)
            num = re.match(r"^\s*(\d+)\.", line).group(1)
            pdf.set_x(pdf.l_margin + 4)
            pdf.set_font("Helvetica", "B", 10.5)
            pdf.set_text_color(*ORANGE)
            pdf.cell(6, 6, f"{num}.")
            pdf.set_text_color(35, 35, 35)
            for chunk, bold in clean_bold(text):
                pdf.set_font("Helvetica", "B" if bold else "", 10.5)
                pdf.write(6, chunk)
            pdf.ln(6.5)
            i += 1
            continue

        if line.startswith(">"):
            pdf.set_font("Helvetica", "I", 9.5)
            pdf.set_text_color(*GRAY)
            pdf.multi_cell(0, 5.6, sanitize(line.lstrip("> ").strip()))
            pdf.ln(1)
            i += 1
            continue

        # párrafo normal
        write_rich_line(pdf, line.strip())
        i += 1


def convert(md_filename, pdf_filename, title):
    md_path = os.path.join(DOCS, md_filename)
    with open(md_path, encoding="utf-8") as f:
        content = f.read()
    pdf = ConstructopolisPDF(title)
    pdf.add_page()
    render_markdown(pdf, content)
    out_path = os.path.join(OUT, pdf_filename)
    pdf.output(out_path)
    size = os.path.getsize(out_path)
    print(f"{pdf_filename}: {size} bytes, {pdf.page_no()} páginas")


def main():
    convert("MEMORIA_DESCRIPTIVA.md", "MEMORIA_DESCRIPTIVA.pdf", "Memoria Descriptiva")
    convert("MANUAL_USUARIO.md", "MANUAL_USUARIO.pdf", "Manual de Usuario")
    convert("MANUAL_TECNICO.md", "MANUAL_TECNICO.pdf", "Manual Técnico")


if __name__ == "__main__":
    main()

"""Approval sheet: a few cultures, each in six looks (man and woman everyday, worker, ruler, warrior, priest).

    /root/bpyenv/bin/python sampler.py OUT.png "Culture A" "Culture B" ...
"""
import csv, subprocess, sys, tempfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
from PIL import Image, ImageDraw
import clothing

COLS = [("male", 0, "man"), ("female", 0, "woman"), ("male", 2, "worker"), ("male", "ruler", "ruler"),
        ("female", "warrior", "warrior"), ("female", "priest", "priest")]


def main():
    out, names = Path(sys.argv[1]), sys.argv[2:]
    rows = {r["culture"]: r for r in csv.DictReader(open(HERE.parent / "culture_dress.csv", newline="", encoding="utf-8"))}
    ao = clothing.load_ao(HERE / "ao.png")
    tmp = Path(tempfile.mkdtemp())
    texs = []
    for i, n in enumerate(names):
        for gender, look, _ in COLS:
            p = tmp / f"c{i}_{gender}_{look}.png"
            clothing.render(rows[n], gender, look, ao).save(p)
            texs.append(p)
    subprocess.run([sys.executable, str(HERE / "preview.py"), str(tmp / "v")] + [str(t) for t in texs], check=True, capture_output=True)
    cw, ch, lw = 200, 325, 230
    sheet = Image.new("RGB", (lw + cw * len(COLS), 30 + ch * len(names)), (58, 64, 72))
    d = ImageDraw.Draw(sheet)
    for j, (_, _, label) in enumerate(COLS):
        d.text((lw + j * cw + 80, 8), label, fill=(230, 226, 214))
    for i, n in enumerate(names):
        words, line, lines = n.split(), "", []
        for w in words:
            if len(line) + len(w) > 30:
                lines.append(line); line = ""
            line += w + " "
        lines.append(line)
        for k, l in enumerate(lines[:6]):
            d.text((10, 30 + i * ch + 130 + 14 * k), l, fill=(230, 226, 214))
        for j, (gender, look, _) in enumerate(COLS):
            im = Image.open(tmp / "v" / f"c{i}_{gender}_{look}_front.png").convert("RGBA").resize((cw, ch), Image.LANCZOS)
            sheet.paste(im, (lw + j * cw, 30 + i * ch), im)
    sheet.save(out)


if __name__ == "__main__":
    main()

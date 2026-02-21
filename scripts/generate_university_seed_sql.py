# Usage: python3 generate_university_seed_sql.py > ../src/main/resources/seed/university_seed.sql
import csv
from pathlib import Path

BASE = Path(__file__).resolve().parent
u = (BASE / "../src/main/resources/seed/university.csv").resolve()
d = (BASE / "../src/main/resources/seed/university_domain.csv").resolve()

def q(s):
    if s is None:
        return "NULL"
    s = s.strip()
    if not s:
        return "NULL"
    return "'" + s.replace("'", "''") + "'"

print("START TRANSACTION;")
print()

print("-- university upsert (MySQL 8 deprecation 대응)")
rows = []
for r in csv.DictReader(u.open(encoding="utf-8-sig")):
    name = (r.get("name") or "").strip()
    if not name:
        continue

    is_active = "TRUE" if (r.get("is_active", "true") or "").strip().lower() == "true" else "FALSE"

    rows.append(
        f"({q(name)}, {q(r.get('acronym', ''))}, {q(r.get('region', ''))}, "
        f"{is_active}, NOW(), NOW())"
    )

if rows:
    print("INSERT INTO university (name, acronym, region, is_active, created_at, updated_at)")
    print("VALUES")
    print(",\n".join(rows))
    print("AS new")
    print("ON DUPLICATE KEY UPDATE")
    print("  acronym = new.acronym,")
    print("  region = new.region,")
    print("  is_active = new.is_active,")
    print("  updated_at = NOW();")
print()

print("-- university_domain upsert (VALUES() 미사용)")
seen_domains = set()
for r in csv.DictReader(d.open(encoding="utf-8-sig")):
    name = (r.get("university_name") or "").strip()
    domain = (r.get("domain") or "").strip().lower().lstrip("@")
    category = ((r.get("category") or "STUDENT").strip().upper()) or "STUDENT"

    if not name or not domain:
        continue
    if domain in seen_domains:
        continue
    seen_domains.add(domain)

    name_esc = name.replace("'", "''")
    domain_esc = domain.replace("'", "''")
    category_esc = category.replace("'", "''")

    print("INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)")
    print(f"SELECT u.id, '{domain_esc}', '{category_esc}', NOW(), NOW()")
    print("FROM university u")
    print(f"WHERE u.name = '{name_esc}'")
    print("ON DUPLICATE KEY UPDATE")
    print(f"  university_id = (SELECT id FROM university WHERE name = '{name_esc}'),")
    print(f"  category = '{category_esc}',")
    print("  updated_at = NOW();")
    print()

print("COMMIT;")
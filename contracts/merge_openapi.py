#!/usr/bin/env python3
"""Fusionne plusieurs fragments OpenAPI en un contrat unique.

Usage :
  python merge_openapi.py -o academic-openapi.yaml students-courses.yaml fragment-membre2.yaml

Règles : paths / components / tags sont fusionnés ; un doublon DIFFÉRENT
(même chemin ou même schéma avec un contenu différent) arrête la fusion
avec une erreur pour que les deux membres tranchent.
"""
import argparse
import sys
import yaml

INFO = {
    "title": "EduCampus - Academic Service API",
    "description": "Contrat REST final du service Academic (students, courses, "
                   "enrollments, schedules, exams, results). Versionnement /api/v1.",
    "version": "1.0.0",
}
SERVERS = [{"url": "http://localhost:8081", "description": "Environnement local"}]


def merge_map(target, source, label):
    for key, value in (source or {}).items():
        if key in target and target[key] != value:
            sys.exit(f"CONFLIT dans {label} : '{key}' est défini différemment dans deux fragments.")
        target[key] = value


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("fragments", nargs="+")
    parser.add_argument("-o", "--output", default="academic-openapi.yaml")
    args = parser.parse_args()

    merged = {"openapi": "3.0.3", "info": INFO, "servers": SERVERS,
              "tags": [], "paths": {}, "components": {}}
    tag_names = set()

    for fragment in args.fragments:
        with open(fragment, encoding="utf-8") as f:
            doc = yaml.safe_load(f)
        for tag in doc.get("tags", []):
            if tag["name"] not in tag_names:
                tag_names.add(tag["name"])
                merged["tags"].append(tag)
        merge_map(merged["paths"], doc.get("paths"), "paths")
        for section, content in (doc.get("components") or {}).items():
            merged["components"].setdefault(section, {})
            merge_map(merged["components"][section], content, f"components.{section}")

    with open(args.output, "w", encoding="utf-8") as f:
        yaml.safe_dump(merged, f, sort_keys=False, allow_unicode=True, width=100)
    print(f"OK : {args.output} généré à partir de {len(args.fragments)} fragment(s).")


if __name__ == "__main__":
    main()

#!/usr/bin/env bash
set -euo pipefail

BASE_REF="${BASE_REF:-}"
if [[ -z "$BASE_REF" ]]; then echo "BASE_REF is required."; exit 1; fi

git fetch origin "$BASE_REF" --depth=1 >/dev/null 2>&1 || true
changed="$(git diff --name-only "origin/$BASE_REF...HEAD")"

if ! echo "$changed" | grep -qE "^app/"; then
  echo "No app source change detected; feature documentation gate not required."
  exit 0
fi

branch_name="${BRANCH_NAME:-$(git branch --show-current)}"
if [[ "$branch_name" != feature/* ]]; then
  echo "App code changed on $branch_name. Feature-documentation gate applies to feature/* branches only."
  exit 0
fi

feature_docs="$(echo "$changed" | grep -E "^docs/features/[^/]+\.md$" | grep -v "FEATURE_TEMPLATE.md" || true)"
if [[ -z "$feature_docs" ]]; then
  echo "::error::A feature/* branch changed app code but did not change a feature specification under docs/features/."
  echo "Create/update a feature spec using docs/features/FEATURE_TEMPLATE.md before merging."
  exit 1
fi

required=("## 1. কেন" "## 2. কী করবে" "## 3. কোথায়" "## 4. কীভাবে কাজ করবে" "## 5. Data" "## 6. Accounting impact" "## 7. Existing feature relationship" "## 8. Validation ও Error" "## 9. Backward compatibility" "## 10. Test plan" "## 11. Acceptance criteria" "## 12. Change history")
while IFS= read -r file; do
  [[ -z "$file" ]] && continue
  for heading in "${required[@]}"; do
    if ! grep -Fq "$heading" "$file"; then
      echo "::error file=$file::Missing required feature-spec section: $heading"
      exit 1
    fi
  done
done <<< "$feature_docs"

echo "Feature documentation gate passed."

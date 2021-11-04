Custom User Profile Extensions
---

# Age Validator

```json
{
  "selector": {
    "scopes": []
  },
  "permissions": {
    "view": [],
    "edit": []
  },
  "name": "dateOfBirth",
  "displayName": "Birthdate",
  "validations": {
    "local-date": {},
    "custom-age": { "min-age": 18, "max-age":69 }
  }
}
```
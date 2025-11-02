# PULL REQUEST TEMPLATE

## Conventional Commit Message Format
Use the following format for all commits included in this PR:
`<type>(<scope>): <short description>`

**type** → what kind of change you made  
**scope** → which part of the project it affects  
**description** → what you actually did  

---

## Type of Change
Select all that apply:

- [ ] feat (new feature or class)
- [ ] test (new or improved tests)
- [ ] refactor (code reorganization, no behavior change)
- [ ] docs (documentation or templates)
- [ ] chore (non-code maintenance tasks)

---

## Scope
Describe which part of the codebase this PR touches  
(e.g., Shape class, CircleTest, LoginPresenter, etc.)

---

## Description
Briefly explain what this pull request changes and why.  
Include context or motivation behind the change if relevant.

---

## Self-Review Checklist
- [ ] Code compiles and runs (`javac` / `java`)
- [ ] All tests pass locally
- [ ] Clear naming and proper access modifiers
- [ ] No commented-out or unused code
- [ ] Follows Conventional Commit style
- [ ] Partner reviewed my code before PR

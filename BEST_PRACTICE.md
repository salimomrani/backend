# Best Practices - Universal Development Guidelines

> **Note:** This document contains universal best practices applicable to any software project (frontend, backend, mobile, etc.). These guidelines are technology-agnostic and can be shared across teams and projects.

---

## 📝 Commit Messages

### Structure
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style/formatting (no logic change)
- `refactor` - Code refactoring
- `perf` - Performance improvements
- `test` - Adding or updating tests
- `chore` - Build process, dependencies, tooling
- `revert` - Revert previous commit

### Rules
1. **Subject line (50 chars max)**
    - Use imperative mood: "add feature" not "added feature"
    - Don't capitalize first letter
    - No period at the end

2. **Body (72 chars per line)**
    - Explain what and why, not how
    - Separate from subject with blank line

3. **Footer**
    - Reference issues: `Fixes #123`, `Closes #456`
    - Breaking changes: `BREAKING CHANGE: description`

### Examples

**Good:**
```
feat(auth): add JWT token refresh mechanism

Implement automatic token refresh to improve user experience
and reduce authentication errors. Token refreshes 5 minutes
before expiration.

Closes #234
```

**Bad:**
```
Updated stuff
Fixed bug
WIP
```

---

## 🌿 Git Best Practices

### Branch Naming
- `feature/short-description` - New features
- `fix/issue-description` - Bug fixes
- `hotfix/critical-issue` - Production hotfixes
- `refactor/component-name` - Code refactoring
- `docs/what-changed` - Documentation updates
- `test/what-tested` - Test additions

### General Rules
1. **Keep branches short-lived** - Merge frequently
2. **One concern per branch** - Single feature/fix per branch
3. **Update frequently** - Pull latest changes daily
4. **Clean history** - Squash unnecessary commits before merge
5. **Delete merged branches** - Keep repository clean

### Before Committing
- [ ] Code compiles/builds successfully
- [ ] All tests pass
- [ ] Linting passes
- [ ] No commented-out code
- [ ] No debug statements (console.log, print, etc.)
- [ ] No sensitive data (passwords, keys, tokens)

### Commit Frequency
- **Commit often** - Small, logical units of work
- **One logical change per commit** - Easy to review and revert
- **Working state** - Each commit should leave code in working state

---

## 🔍 Code Quality

### General Principles

1. **SOLID Principles**
    - Single Responsibility
    - Open/Closed
    - Liskov Substitution
    - Interface Segregation
    - Dependency Inversion

2. **DRY (Don't Repeat Yourself)**
    - Extract duplicated code into functions/methods
    - Use configuration files for repeated values
    - Create reusable components/modules

3. **KISS (Keep It Simple, Stupid)**
    - Simplest solution is often the best
    - Avoid over-engineering
    - Write code for humans, not machines

4. **YAGNI (You Aren't Gonna Need It)**
    - Don't implement features "just in case"
    - Add functionality when actually needed

### Code Organization

1. **Consistent naming conventions**
    - Variables: descriptive, meaningful names
    - Functions: verb-based names (getUserData, calculateTotal)
    - Classes: noun-based names (UserService, ArticleController)
    - Constants: UPPER_SNAKE_CASE

2. **File organization**
    - One class/component per file
    - Group related files together
    - Consistent folder structure
    - Separate concerns (business logic, presentation, data access)

3. **Function/Method size**
    - Keep functions small and focused
    - Ideal: < 20 lines
    - Maximum: 50 lines
    - Extract complex logic into separate functions

4. **Avoid deep nesting**
    - Maximum 3 levels of indentation
    - Use early returns
    - Extract nested logic into separate functions

---

## 📚 Documentation

### What to Document

1. **Code-level documentation**
    - Public APIs and interfaces
    - Complex algorithms
    - Non-obvious business logic
    - Workarounds and hacks (with explanation)

2. **README.md** (every project must have)
    - Project description
    - Prerequisites
    - Installation instructions
    - Usage examples
    - Configuration
    - Contributing guidelines
    - License

3. **Architecture documentation**
    - High-level system design
    - Component interactions
    - Data flow diagrams
    - Technology stack

4. **API documentation**
    - Endpoints and methods
    - Request/response formats
    - Authentication requirements
    - Error codes

### Documentation Best Practices

- **Keep it up to date** - Update docs with code changes
- **Use examples** - Show, don't just tell
- **Be concise** - Clear and to the point
- **Use diagrams** - Visual aids for complex concepts
- **Version documentation** - Match docs to code versions

### Comments Best Practices

**Good comments explain WHY:**
```
// Cache results for 5 minutes to reduce database load
// Customer requested this specific timeout value
```

**Bad comments explain WHAT (code should be self-explanatory):**
```
// Increment counter by 1
counter++;
```

**When to comment:**
- Complex business rules
- Performance optimizations
- Security considerations
- Workarounds for external issues
- TODO/FIXME with context

**When NOT to comment:**
- Obvious code
- Redundant information
- Outdated comments (delete them!)

---

## 🧪 Testing

### Testing Pyramid

```
        /\
       /  \       E2E Tests (10%)
      /____\      - Expensive, slow, brittle
     /      \
    /        \    Integration Tests (20%)
   /__________\   - Test component interactions
  /            \
 /              \ Unit Tests (70%)
/________________\- Fast, reliable, cheap
```

### Testing Principles

1. **Write testable code**
    - Small, focused functions
    - Dependency injection
    - Avoid global state
    - Pure functions when possible

2. **Test behavior, not implementation**
    - Test public APIs
    - Don't test private methods directly
    - Refactoring shouldn't break tests

3. **Arrange-Act-Assert (AAA) pattern**
   ```
   // Arrange - Set up test data
   // Act - Execute the code being tested
   // Assert - Verify the result
   ```

4. **Test names should describe behavior**
    - Good: `should_return_error_when_user_not_found`
    - Bad: `test1`, `testGetUser`

5. **One assertion per test** (when practical)
    - Easier to identify failures
    - More maintainable

6. **Independent tests**
    - Tests should not depend on each other
    - Can run in any order
    - Isolated test data

### Code Coverage

- **Aim for 70-80%** minimum coverage
- **100% coverage ≠ bug-free** code
- **Focus on critical paths** first
- **Don't test for the sake of coverage**

### What to Test

✅ **Test:**
- Business logic
- Edge cases
- Error handling
- User workflows
- API contracts

❌ **Don't test:**
- Framework code
- Third-party libraries
- Trivial getters/setters
- Configuration files

---

## 🔒 Security Best Practices

### General Rules

1. **Never commit secrets**
    - No passwords, API keys, tokens in code
    - Use environment variables
    - Use secret management tools
    - Add sensitive files to .gitignore

2. **Input validation**
    - Validate all user input
    - Sanitize data before processing
    - Use allowlists, not denylists

3. **Authentication & Authorization**
    - Use established libraries/frameworks
    - Never roll your own crypto
    - Implement proper session management
    - Use secure password hashing (bcrypt, argon2)

4. **Data protection**
    - Encrypt sensitive data at rest
    - Use HTTPS/TLS for data in transit
    - Implement proper CORS policies
    - Protect against injection attacks

5. **Error handling**
    - Don't expose sensitive info in errors
    - Log security events
    - Use generic error messages for users

6. **Dependencies**
    - Keep dependencies up to date
    - Audit dependencies regularly
    - Remove unused dependencies
    - Use known, trusted packages

### Security Checklist

- [ ] All user input is validated
- [ ] Authentication is implemented correctly
- [ ] Sensitive data is encrypted
- [ ] Error messages don't leak information
- [ ] Dependencies are up to date
- [ ] HTTPS is enforced
- [ ] Security headers are configured
- [ ] No secrets in code/config

---

## ⚡ Performance Best Practices

### General Principles

1. **Measure before optimizing**
    - Profile to find bottlenecks
    - Don't optimize prematurely
    - Measure impact of changes

2. **Optimize the critical path first**
    - Focus on user-facing features
    - Optimize frequent operations
    - Defer non-critical work

3. **Use appropriate data structures**
    - Choose right data structure for use case
    - Consider time/space complexity
    - Use built-in optimized structures

4. **Caching strategies**
    - Cache expensive computations
    - Invalidate cache appropriately
    - Use appropriate cache levels

5. **Database optimization**
    - Use indexes on frequently queried columns
    - Optimize queries (avoid N+1)
    - Use pagination for large datasets
    - Connection pooling

6. **Resource management**
    - Close resources properly
    - Avoid memory leaks
    - Use lazy loading when appropriate

---

## 👥 Code Review Best Practices

### For Authors

1. **Before requesting review**
    - Self-review your code first
    - Run all tests
    - Check code style/linting
    - Write clear PR description
    - Keep PRs small (< 400 lines)

2. **PR description should include**
    - What changed and why
    - How to test
    - Screenshots (for UI changes)
    - Related issues/tickets

3. **Respond to feedback**
    - Be open to suggestions
    - Ask questions if unclear
    - Don't take criticism personally
    - Update code based on feedback

### For Reviewers

1. **Focus on**
    - Logic errors and bugs
    - Code readability
    - Test coverage
    - Security issues
    - Performance concerns
    - Architecture/design

2. **Don't focus on**
    - Personal style preferences
    - Nitpicking formatting (use linters)
    - Rewriting everything your way

3. **Be constructive**
    - Explain WHY, not just WHAT
    - Suggest alternatives
    - Ask questions, don't demand
    - Praise good code

4. **Review promptly**
    - Within 24 hours ideally
    - Don't let PRs go stale
    - Block for critical issues only

### Review Checklist

- [ ] Code is readable and maintainable
- [ ] Business logic is correct
- [ ] Edge cases are handled
- [ ] Tests are adequate
- [ ] No security vulnerabilities
- [ ] Performance is acceptable
- [ ] Documentation is updated
- [ ] No debugging code left

---

## 🚀 CI/CD Principles

### Continuous Integration

1. **Commit frequently**
    - Integrate code daily minimum
    - Small, incremental changes
    - Reduces merge conflicts

2. **Automated builds**
    - Build on every commit
    - Fast feedback loop
    - Fail fast

3. **Automated testing**
    - Run tests on every build
    - Block merge if tests fail
    - Maintain fast test suite

4. **Code quality checks**
    - Linting and formatting
    - Code coverage metrics
    - Static analysis

### Continuous Deployment

1. **Automate deployments**
    - Repeatable process
    - No manual steps
    - Rollback capability

2. **Environment parity**
    - Dev, staging, prod similar
    - Use same deployment process
    - Infrastructure as code

3. **Feature flags**
    - Deploy without releasing
    - Gradual rollouts
    - Easy rollback

4. **Monitoring**
    - Health checks
    - Error tracking
    - Performance monitoring
    - Alerting

---

## 🐛 Error Handling

### Principles

1. **Fail fast, fail loudly**
    - Detect errors early
    - Don't hide failures
    - Log errors appropriately

2. **Graceful degradation**
    - Handle errors gracefully
    - Provide fallback behavior
    - Inform users appropriately

3. **Meaningful error messages**
    - For developers: detailed, technical
    - For users: friendly, actionable
    - Never expose internal details to users

4. **Logging best practices**
    - Log errors with context
    - Use appropriate log levels
    - Include timestamps and stack traces
    - Don't log sensitive data

### Error Handling Patterns

1. **Try-Catch appropriately**
    - Catch specific exceptions
    - Don't catch and ignore
    - Re-throw when appropriate
    - Clean up resources in finally

2. **Return codes vs Exceptions**
    - Use exceptions for exceptional cases
    - Return codes for expected errors
    - Be consistent in approach

3. **Error recovery**
    - Retry transient failures
    - Implement circuit breakers
    - Provide fallback options

---

## 📊 Monitoring & Logging

### What to Monitor

1. **Application metrics**
    - Response times
    - Error rates
    - Request volumes
    - Resource usage

2. **Business metrics**
    - User actions
    - Feature usage
    - Conversion rates

3. **Infrastructure metrics**
    - CPU, memory, disk usage
    - Network performance
    - Database performance

### Logging Levels

- **ERROR** - Something failed, needs attention
- **WARN** - Potential issue, monitor closely
- **INFO** - Important business events
- **DEBUG** - Detailed diagnostic information
- **TRACE** - Very detailed information

### Logging Best Practices

1. **Structured logging**
    - Use JSON format
    - Include correlation IDs
    - Add context (user, session, etc.)

2. **Log retention**
    - Define retention policy
    - Archive old logs
    - Comply with regulations

3. **Don't log**
    - Passwords or secrets
    - PII without consent
    - Excessive data in production

---

## 📖 Additional Resources

### Books
- Clean Code by Robert C. Martin
- The Pragmatic Programmer by Hunt & Thomas
- Refactoring by Martin Fowler
- Design Patterns by Gang of Four

### Websites
- [12 Factor App](https://12factor.net/)
- [Git Best Practices](https://sethrobertson.github.io/GitBestPractices/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)

---

## ✅ Quick Checklist

Before every commit:
- [ ] Code builds/compiles
- [ ] All tests pass
- [ ] Linting passes
- [ ] No console logs or debug code
- [ ] No commented code
- [ ] No secrets in code
- [ ] Commit message follows convention
- [ ] Self-reviewed changes

Before every PR:
- [ ] All commits checklist items ✅
- [ ] PR description is clear
- [ ] Tests added for new features
- [ ] Documentation updated
- [ ] Small, focused changes
- [ ] Ready for review

---

**Remember:** These are guidelines, not absolute rules. Use your judgment and adapt to your project's needs.

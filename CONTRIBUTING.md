# Contributing to the Identity SDK for Android

## Code of conduct
The project is governed by the [Code of Conduct](CODE_OF_CONDUCT.md), adapted from [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/version/1/4/code-of-conduct.html). Please report any unacceptable behavior to [support@spid.no](mailto:support@spid.no)

## License
The Account SDK for Android is released under the [MIT License](LICENSE). Any code submitted will be licensed under the same conditions unless explicitly stated otherwise.

## Practical information
First of all, you should ensure your environment is set up in compliance with the _ktlint_ style guide. The steps to do this, can be found over at their [Github repo](https://github.com/shyiko/ktlint#option-1-recommended). In our repo, `master` is always the primary branch. To contribute, please make a PR against this branch from your own fork, or another branch if you have write access (which we are happy to grant to contributors).

Commit messages should clearly reflect it's changes in the format **If applied, this commit will &lt;your changes&gt;>**. Some examples include:
- Add events hooks to the UI module
- Fix an issue where token refreshing failed
- Improve the logging in the AuthInterceptor class

A good example of a commit message can be found over at https://git-scm.com/book/en/v2/Distributed-Git-Contributing-to-a-Project. 

```
Short (50 chars or less) summary of changes

More detailed explanatory text, if necessary.  Wrap it to
about 72 characters or so.  In some contexts, the first
line is treated as the subject of an email and the rest of
the text as the body.  The blank line separating the
summary from the body is critical (unless you omit the body
entirely); tools like rebase can get confused if you run
the two together.

Further paragraphs come after blank lines.

  - Bullet points are okay, too

  - Typically a hyphen or asterisk is used for the bullet,
    preceded by a single space, with blank lines in
    between, but conventions vary here
```

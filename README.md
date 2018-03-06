# account-sdk-android gh-pages


## Getting started
1. Ensure that your requirements are met and install missing requirements
    1. Ensure that Ruby &gt; 2.2.5 is installed by running the command `ruby -v`
        - Install: `sudo apt-get install ruby-full`
    2. Ensure that RubyGems is installed by running `gem -v`
        - Install: `sudo apt-get install rubygems`
    3. Ensure that GCC nad Make is installed by running `gcc -v`, `g++ -v` and `make -v`
        - Install: `sudo apt-get install g++ gcc make`
2. Install the Jekyll and Bundler gems: `gem install jekyll bundler`
3. Install gems using bundler: `bundle install`
4. Launch the site locally by running `bundle exec jekyll serve`
    - Pro tip #1: Launch Jekyll with the `--watch` argument to automatically rebuild the site when it changes
    - Pro tip #2: Launch Jekyll with the `--incremental` argument to increase build speed

# Makefile to run and deploy my portfolio, and git shortcuts
# Mario Jiménez <mario.emilio.j@gmail.com>

# Usage:
# make run      # run portfolio
# make deploy   # deploy the portfolio (inside Google Cloud Shell)
# make update   # fetch branches from git and fast-forward the main branch

all: run

compile:
	cd portfolio && mvn package

run: compile
	cd portfolio && mvn exec:java

deploy: compile
	cd portfolio && mvn appengine:deploy

update:
	git fetch --all --prune
	git checkout main
	git pull --rebase --autostash

.PHONY = all compile run deploy update

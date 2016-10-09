# ------------------------------------------------------------------------------

default: build

# SPEC -------------------------------------------------------------------------

NAME    := robinmap
VERSION := 0.1.0

# PATHS ------------------------------------------------------------------------

ifeq ($(OS) , Windows_NT)
SEP := ;
else
SEP := :
endif

output  := out/production/$(NAME)
test    := out/test/$(NAME)
cp      := "$(output)"
testcp  := "$(output)$(SEP)$(test)"

# CLEAN ------------------------------------------------------------------------

clean:
	rm -rf out

# BUILD & TEST -----------------------------------------------------------------

build:
	mkdir -p $(output)
	kotlinc -cp $(cp) src -d $(output)
	mkdir -p $(test)
	kotlinc -cp $(testcp) test -d $(test)

test:
	kotlin -cp $(testcp) norswap.triemap.TestKt

# JAR --------------------------------------------------------------------------

jar:
	find out -name .DS_Store -type f -delete
	jar cf out/$(NAME)-$(VERSION).jar -C $(output) .

# PROFILE ----------------------------------------------------------------------

klib   := $(realpath $(dir $(shell which kotlin))../lib)
javacp := "$(output)$(SEP)$(test)$(SEP)$(klib)/kotlin-runtime.jar"

trace:
	java -cp $(javacp) -agentlib:hprof=cpu=samples,interval=1 $(target)

# ------------------------------------------------------------------------------

.PHONY: \
  default \
  clean \
  build \
  build-tests \
  test \
  jar

# ------------------------------------------------------------------------------

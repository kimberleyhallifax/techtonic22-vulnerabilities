FROM returntocorp/semgrep

RUN apk add --no-cache curl \
    && curl -sSfL https://raw.githubusercontent.com/anchore/grype/main/install.sh | sh -s -- -b /usr/local/bin \
    && curl -sSfL https://raw.githubusercontent.com/anchore/syft/main/install.sh | sh -s -- -b /usr/local/bin 

WORKDIR /techtonic22-vulnerabilities

ENTRYPOINT [ "/bin/sh" ]
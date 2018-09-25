FROM richarvey/nginx-php-fpm:1.3.10

#tetsuobe/php7:v7.1.12
ENV BUILD_DEPS1 \
                zlib-dev \
                icu-dev \
                util-linux-dev \
                autoconf \
                g++ \
                gcc \
                make \
                pcre-dev

RUN apk update && apk add --no-cache --virtual .build-deps $BUILD_DEPS1 \
    && apk add bash icu-libs util-linux \
    && docker-php-ext-install zip mbstring intl opcache bcmath \
    && pecl install -o xdebug \
    && pecl install -o apcu \
    && pecl install -o uuid \
    && docker-php-ext-enable xdebug apcu uuid \
    && apk del .build-deps \
    && rm -rf /tmp/pear

RUN curl -sS https://getcomposer.org/installer | php \
    && mv composer.phar /usr/bin/composer


#ulff/php7-cassandra-dockerimage
ENV INSTALL_DIR /usr/src/datastax-php-driver

ENV BUILD_DEPS2 \
                cmake \
                autoconf \
                g++ \
                gcc \
                make \
                pcre-dev \
                openssl-dev \
                libuv-dev \
                gmp-dev

RUN apk update && apk add --no-cache --virtual .build-deps $BUILD_DEPS2 \
    && apk add --no-cache git libuv gmp \
    && git clone https://github.com/datastax/php-driver.git $INSTALL_DIR \
    && cd $INSTALL_DIR \
    && git submodule update --init \
    && cd ext && ./install.sh \
    && docker-php-ext-enable cassandra \
    && apk del .build-deps \
    && rm -rf $INSTALL_DIR

#amimof/sftp
COPY sshd_config /etc/ssh/sshd_config

RUN set -x \
&&  apk add --no-cache --update openssh bash vim\
&&  mkdir -p /var/run/sshd


# override entrypoint script of richarvey/nginx-php-fpm:1.3.10
COPY start.sh /start.sh
RUN chmod 755 /start.sh

ADD src/ /var/www/html/

COPY supervisord.conf /etc/supervisord.conf



#!/bin/bash

# Disable Strict Host checking for non interactive git clones

mkdir -p -m 0700 /root/.ssh
# Prevent config files from being filled to infinity by force of stop and restart the container 
echo "" > /root/.ssh/config
echo -e "Host *\n\tStrictHostKeyChecking no\n" >> /root/.ssh/config

if [[ "$GIT_USE_SSH" == "1" ]] ; then
  echo -e "Host *\n\tUser ${GIT_USERNAME}\n\n" >> /root/.ssh/config
fi

if [ ! -z "$SSH_KEY" ]; then
 echo $SSH_KEY > /root/.ssh/id_rsa.base64
 base64 -d /root/.ssh/id_rsa.base64 > /root/.ssh/id_rsa
 chmod 600 /root/.ssh/id_rsa
fi

# Set custom webroot
if [ ! -z "$WEBROOT" ]; then
 sed -i "s#root /var/www/html;#root ${WEBROOT};#g" /etc/nginx/sites-available/default.conf
else
 webroot=/var/www/html
fi

# Setup git variables
if [ ! -z "$GIT_EMAIL" ]; then
 git config --global user.email "$GIT_EMAIL"
fi
if [ ! -z "$GIT_NAME" ]; then
 git config --global user.name "$GIT_NAME"
 git config --global push.default simple
fi

# Dont pull code down if the .git folder exists
if [ ! -d "/var/www/html/.git" ]; then
 # Pull down code from git for our site!
 if [ ! -z "$GIT_REPO" ]; then
   # Remove the test index file if you are pulling in a git repo
   if [ ! -z ${REMOVE_FILES} ] && [ ${REMOVE_FILES} == 0 ]; then
     echo "skiping removal of files"
   else
     rm -Rf /var/www/html/*
   fi
   GIT_COMMAND='git clone '
   if [ ! -z "$GIT_BRANCH" ]; then
     GIT_COMMAND=${GIT_COMMAND}" -b ${GIT_BRANCH}"
   fi

   if [ -z "$GIT_USERNAME" ] && [ -z "$GIT_PERSONAL_TOKEN" ]; then
     GIT_COMMAND=${GIT_COMMAND}" ${GIT_REPO}"
   else
    if [[ "$GIT_USE_SSH" == "1" ]]; then
      GIT_COMMAND=${GIT_COMMAND}" ${GIT_REPO}"
    else
      GIT_COMMAND=${GIT_COMMAND}" https://${GIT_USERNAME}:${GIT_PERSONAL_TOKEN}@${GIT_REPO}"
    fi
   fi
   ${GIT_COMMAND} /var/www/html || exit 1
   if [ -z "$SKIP_CHOWN" ]; then
     chown -Rf nginx.nginx /var/www/html
   fi
 fi
fi

# Enable custom nginx config files if they exist
if [ -f /var/www/html/conf/nginx/nginx.conf ]; then
  cp /var/www/html/conf/nginx/nginx.conf /etc/nginx/nginx.conf
fi

if [ -f /var/www/html/conf/nginx/nginx-site.conf ]; then
  cp /var/www/html/conf/nginx/nginx-site.conf /etc/nginx/sites-available/default.conf
fi

if [ -f /var/www/html/conf/nginx/nginx-site-ssl.conf ]; then
  cp /var/www/html/conf/nginx/nginx-site-ssl.conf /etc/nginx/sites-available/default-ssl.conf
fi


# Prevent config files from being filled to infinity by force of stop and restart the container
lastlinephpconf="$(grep "." /usr/local/etc/php-fpm.conf | tail -1)"
if [[ $lastlinephpconf == *"php_flag[display_errors]"* ]]; then
 sed -i '$ d' /usr/local/etc/php-fpm.conf
fi

# Display PHP error's or not
if [[ "$ERRORS" != "1" ]] ; then
 echo php_flag[display_errors] = off >> /usr/local/etc/php-fpm.conf
else
 echo php_flag[display_errors] = on >> /usr/local/etc/php-fpm.conf
fi

# Display Version Details or not
if [[ "$HIDE_NGINX_HEADERS" == "0" ]] ; then
 sed -i "s/server_tokens off;/server_tokens on;/g" /etc/nginx/nginx.conf
else
 sed -i "s/expose_php = On/expose_php = Off/g" /usr/local/etc/php-fpm.conf
fi

# Pass real-ip to logs when behind ELB, etc
if [[ "$REAL_IP_HEADER" == "1" ]] ; then
 sed -i "s/#real_ip_header X-Forwarded-For;/real_ip_header X-Forwarded-For;/" /etc/nginx/sites-available/default.conf
 sed -i "s/#set_real_ip_from/set_real_ip_from/" /etc/nginx/sites-available/default.conf
 if [ ! -z "$REAL_IP_FROM" ]; then
  sed -i "s#172.16.0.0/12#$REAL_IP_FROM#" /etc/nginx/sites-available/default.conf
 fi
fi
# Do the same for SSL sites
if [ -f /etc/nginx/sites-available/default-ssl.conf ]; then
 if [[ "$REAL_IP_HEADER" == "1" ]] ; then
  sed -i "s/#real_ip_header X-Forwarded-For;/real_ip_header X-Forwarded-For;/" /etc/nginx/sites-available/default-ssl.conf
  sed -i "s/#set_real_ip_from/set_real_ip_from/" /etc/nginx/sites-available/default-ssl.conf
  if [ ! -z "$REAL_IP_FROM" ]; then
   sed -i "s#172.16.0.0/12#$REAL_IP_FROM#" /etc/nginx/sites-available/default-ssl.conf
  fi
 fi
fi

#Display errors in docker logs
if [ ! -z "$PHP_ERRORS_STDERR" ]; then
  echo "log_errors = On" >> /usr/local/etc/php/conf.d/docker-vars.ini
  echo "error_log = /dev/stderr" >> /usr/local/etc/php/conf.d/docker-vars.ini
fi

# Increase the memory_limit
if [ ! -z "$PHP_MEM_LIMIT" ]; then
 sed -i "s/memory_limit = 128M/memory_limit = ${PHP_MEM_LIMIT}M/g" /usr/local/etc/php/conf.d/docker-vars.ini
fi

# Increase the post_max_size
if [ ! -z "$PHP_POST_MAX_SIZE" ]; then
 sed -i "s/post_max_size = 100M/post_max_size = ${PHP_POST_MAX_SIZE}M/g" /usr/local/etc/php/conf.d/docker-vars.ini
fi

# Increase the upload_max_filesize
if [ ! -z "$PHP_UPLOAD_MAX_FILESIZE" ]; then
 sed -i "s/upload_max_filesize = 100M/upload_max_filesize= ${PHP_UPLOAD_MAX_FILESIZE}M/g" /usr/local/etc/php/conf.d/docker-vars.ini
fi

# Enable xdebug
XdebugFile='/usr/local/etc/php/conf.d/docker-php-ext-xdebug.ini'
if [[ "$ENABLE_XDEBUG" == "1" ]] ; then
  if [ -f $XdebugFile ]; then
  	echo "Xdebug enabled"
  else
  	echo "Enabling xdebug"
  	echo "If you get this error, you can safely ignore it: /usr/local/bin/docker-php-ext-enable: line 83: nm: not found"
  	# see https://github.com/docker-library/php/pull/420
    docker-php-ext-enable xdebug
    # see if file exists
    if [ -f $XdebugFile ]; then
        # See if file contains xdebug text.
        if grep -q xdebug.remote_enable "$XdebugFile"; then
            echo "Xdebug already enabled... skipping"
        else
            echo "zend_extension=$(find /usr/local/lib/php/extensions/ -name xdebug.so)" > $XdebugFile # Note, single arrow to overwrite file.
            echo "xdebug.remote_enable=1 "  >> $XdebugFile
            echo "xdebug.remote_log=/tmp/xdebug.log"  >> $XdebugFile
            echo "xdebug.remote_autostart=false "  >> $XdebugFile # I use the xdebug chrome extension instead of using autostart
            # NOTE: xdebug.remote_host is not needed here if you set an environment variable in docker-compose like so `- XDEBUG_CONFIG=remote_host=192.168.111.27`.
            #       you also need to set an env var `- PHP_IDE_CONFIG=serverName=docker`
        fi
    fi
  fi
else
    if [ -f $XdebugFile ]; then
        echo "Disabling Xdebug"
      rm $XdebugFile
    fi
fi

if [ ! -z "$PUID" ]; then
  if [ -z "$PGID" ]; then
    PGID=${PUID}
  fi
  deluser nginx
  addgroup -g ${PGID} nginx
  adduser -D -S -h /var/cache/nginx -s /sbin/nologin -G nginx -u ${PUID} nginx
else
  if [ -z "$SKIP_CHOWN" ]; then
    chown -Rf nginx.nginx /var/www/html
  fi
fi

# Run custom scripts
if [[ "$RUN_SCRIPTS" == "1" ]] ; then
  if [ -d "/var/www/html/scripts/" ]; then
    # make scripts executable incase they aren't
    chmod -Rf 750 /var/www/html/scripts/*
    # run scripts in number order
    for i in `ls /var/www/html/scripts/`; do /var/www/html/scripts/$i ; done
  else
    echo "Can't find script directory"
  fi
fi

if [ -z "$SKIP_COMPOSER" ]; then
    # Try auto install for composer
    if [ -f "/var/www/html/composer.lock" ]; then
        if [ "$APPLICATION_ENV" == "development" ]; then
            composer global require hirak/prestissimo
            composer install --working-dir=/var/www/html
        else
            composer global require hirak/prestissimo
            composer install --no-dev --working-dir=/var/www/html
        fi
    fi
fi



# Check if username is set
if [ -z "$SSH_USERNAME" ]; then
  echo "INFO: Username not set. Using default"
  SSH_USERNAME="sftpuser"
fi

# Check if uid/guid is set
if [ -z "$SSH_USERID" ]; then
  echo "INFO: UID/GUID not set. Using default"
  SSH_USERID=1337
fi

# Check if generate hostkeys is set
if [ -z "$SSH_GENERATE_HOSTKEYS" ]; then
  echo "INFO: Generate hostkeys not set. Using default"
  SSH_GENERATE_HOSTKEYS="true"
fi

# Create group
echo "INFO: Adding group ${SSH_USERNAME}"
addgroup -g $SSH_USERID $SSH_USERNAME

# Create user
echo "INFO: Adding user ${SSH_USERNAME}"
adduser -D -u $SSH_USERID -G $SSH_USERNAME $SSH_USERNAME

# Set password if provided
if [ -z "$SSH_PASSWORD" ]; then
  echo "INFO: Password not provided for user ${SSH_USERNAME}"
  passwd -u $SSH_USERNAME
else
  echo "INFO: Setting password for user ${SSH_USERNAME}"
  echo $SSH_USERNAME:$SSH_PASSWORD | chpasswd > /dev/null
  sed -i "s/PasswordAuthentication\s[^ ]*/PasswordAuthentication yes/g" /etc/ssh/sshd_config
fi

# Set Port to listen on
if [ ! -z "$SSH_PORT" ]; then
  echo "INFO: Setting Port to ${SSH_PORT}"
  sed -i "s/Port\s[^ ]*/Port ${SSH_PORT}/g" /etc/ssh/sshd_config
fi

# Change ownership and permissions of users home root dir
echo "INFO: Change ownership and permissions of home directory"
chown root:root /home/$SSH_USERNAME
chmod 755 /home/$SSH_USERNAME

# Set read/write permission for user
#echo "INFO: Set permissions on webroot"
#SSH_DATADIR_NAME="/var/www/html"
#chown $SSH_USERNAME $SSH_DATADIR_NAME
#chmod 777 $SSH_DATADIR_NAME
#adduser -G $SSH_USERNAME www-data

# Create data dir and set read/write permission for user
echo "INFO: Create and set permissions on data dir"
SSH_DATADIR_NAME="data"
mkdir -p /home/$SSH_USERNAME/$SSH_DATADIR_NAME
chown $SSH_USERNAME /home/$SSH_USERNAME/$SSH_DATADIR_NAME
chmod 777 /home/$SSH_USERNAME/$SSH_DATADIR_NAME

# Add SSH keys to authorized_keys with valid permissions
if [ -d /home/$SSH_USERNAME/.ssh/keys ]; then
  echo "INFO: Set ownership and permission of .ssh directory"
  chown -R root:root /home/$SSH_USERNAME/.ssh
  chmod 755 /home/$SSH_USERNAME/.ssh

  echo "INFO: Add SSH keys to authorized_keys with valid permissions"
  cat /home/$SSH_USERNAME/.ssh/keys/* >> /home/$SSH_USERNAME/.ssh/authorized_keys
  chown $SSH_USERNAME:root /home/$SSH_USERNAME/.ssh/authorized_keys
  chmod 644 /home/$SSH_USERNAME/.ssh/authorized_keys
fi

# Generate host keys by default
if [ "${SSH_GENERATE_HOSTKEYS,,}" == "true" ]; then
  echo "INFO: Generating host keys"

  mkdir -p /etc/ssh/host_keys/

  ssh-keygen -f /etc/ssh/host_keys/ssh_host_rsa_key -q -N '' -t rsa
  ln -s /etc/ssh/host_keys/ssh_host_rsa_key /etc/ssh/ssh_host_rsa_key

  ssh-keygen -f /etc/ssh/host_keys/ssh_host_dsa_key -q -N '' -t dsa
  ln -s /etc/ssh/host_keys/ssh_host_dsa_key /etc/ssh/ssh_host_dsa_key

  ssh-keygen -f /etc/ssh/host_keys/ssh_host_ecdsa_key -q -N '' -t ecdsa
  ln -s /etc/ssh/host_keys/ssh_host_ecdsa_key /etc/ssh/ssh_host_ecdsa_key

  ssh-keygen -f /etc/ssh/host_keys/ssh_host_ed25519_key -q -N '' -t ed25519
  ln -s /etc/ssh/host_keys/ssh_host_ed25519_key /etc/ssh/ssh_host_ed25519_key
fi

echo "INFO: Setting permissions on host keys"
chmod 600 /etc/ssh/host_keys/*

# Check for loglevel and replace line in sshd_config
if [ -n "$LOGLEVEL" ]; then
  echo "INFO: Setting LogLevel to ${LOGLEVEL}"
  sed -i "s/LogLevel\s[^ ]*/LogLevel ${LOGLEVEL}/g" /etc/ssh/sshd_config
fi


# Start supervisord and services
exec /usr/bin/supervisord -n -c /etc/supervisord.conf


FROM centos:7
MAINTAINER RightStack <support@rightstack.net>

# set locale ko_KR
RUN localedef -f UTF-8 -i ko_KR ko_KR.UTF-8

ENV LANG ko_KR.UTF-8
ENV LANGUAGE ko_KR.UTF-8
ENV LC_ALL ko_KR.UTF-8

# install jdk
RUN yum -y update && yum clean all
RUN yum -y install java-1.8.0-openjdk-devel.x86_64 && \
    yum clean all

# install subversion 1.9
ADD wandisco-svn.repo /etc/yum.repos.d
RUN yum -y install subversion subversion-javahl && \
    yum clean all

ENV APP_CONFIG_DIR /var/opt/teamcode/config

RUN mkdir -p $APP_CONFIG_DIR
ADD application-production.yml $APP_CONFIG_DIR/application.yml

ADD teamcode-1.7.5.jar app.jar
RUN bash -c 'touch /app.jar'

COPY ./docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ENV TC_UID 578
ENV XMS 2048m
ENV XMX 2048m
RUN groupadd -r teamcode --gid=578 && useradd -r -g teamcode --uid=578 teamcode
USER teamcode

ENTRYPOINT ["/docker-entrypoint.sh"]

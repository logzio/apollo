FROM nginx:1.10.3-alpine
MAINTAINER Roi Rav-Hon <roi@logz.io>
RUN apk add --update \
    openjdk8-jre \
    bash \
  && rm -rf /var/cache/apk/*

ADD https://oss.sonatype.org/content/repositories/public/io/hawt/hawtio-app/1.4.67/hawtio-app-1.4.67.jar /

COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY docker/go.sh /go.sh

COPY ui/dist /usr/share/nginx/html
COPY ui2/build /usr/share/nginx/html/newApollo
COPY apollo-backend/target/apollo-backend-jar-with-dependencies.jar /apollo-backend-jar-with-dependencies.jar
CMD /go.sh

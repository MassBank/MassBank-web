FROM maven:latest

## Build WAR from github repo, keep *.war files and cleanup build environment (for smaller docker image)
## sample/index.html is required for kubernetes livenessProbe
RUN git clone --depth 1 https://github.com/MassBank/MassBank-web.git ; \
    ln -s /MassBank-web/MassBank-Project /project ; \
    mvn clean package -f /project ; \
    mkdir -p /usr/local/tomcat/webapps/sample/ ; \
    touch /usr/local/tomcat/webapps/sample/index.html ;\
    cp /project/MassBank/target/MassBank.war /usr/local/tomcat/webapps/ ; \
    cp /project/api/target/api.war /usr/local/tomcat/webapps/ ; \ 
    cp -avx /project/MassBank-lib/target/MassBank-lib-0.0.1-default/MassBank-lib-0.0.1 / ;\
    rm -rf /MassBank-web .m2 

CMD [ "sh", "-c", "cp -avx /usr/local/tomcat/webapps/* /app ; tail -f /dev/null"]

# This was the manual invocation:
# docker run --rm -v $HOME/.m2:/root/.m2 -v $PWD/MassBank-Project:/project -v $PWD/conf/full-service.conf:/etc/massbank.conf -v $PWD/../MassBank-data:/MassBank-data maven:latest mvn clean package -f /project^C


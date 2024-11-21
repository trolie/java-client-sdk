FROM dig-grid-artifactory.apps.ge.com/ngem-docker/ngem-base-build:ubi-gcc8-corretto-jdk17
ARG ARTIFACTORY_USR
ARG ARTIFACTORY_PSW
ARG SIGNER_KEYSTORE
ARG SIGNER_STORE_PASS
ARG SIGNER_ALIAS
ARG SIGNER_KEY_PASS
ARG BUILD_NUMBER
ARG BUILD_GOAL

COPY . /src
WORKDIR /src
# Log basic build info.
RUN mvn -version
RUN echo "Build Number ${BUILD_NUMBER}"
# Set version
RUN if [ "${BUILD_TARGET}" == "release" ]; then \
        mvn versions:set -DremoveSnapshot -DartifactoryUsr=${ARTIFACTORY_USR} -DartifactoryPsw=${ARTIFACTORY_PSW} -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2; \
    fi
RUN if [ "${BUILD_TARGET}" == "snapshot" ]; then \
        mvn --batch-mode \
        com.ge.energy:ge-versions-maven-plugin:convert-snapshot \
	versions:set \
	-Dtarget.build.number=${BUILD_NUMBER} \
        -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2 \
        -DartifactoryUsr=${ARTIFACTORY_USR} \
        -DartifactoryPsw=${ARTIFACTORY_PSW}; \
    fi


RUN mvn clean ${BUILD_GOAL} \
    -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2 \
	  -Djarsigner.tsa=http://timestamp.digicert.com \
    -DartifactoryUsr=${ARTIFACTORY_USR} \
    -DartifactoryPsw=${ARTIFACTORY_PSW} \
    -Dstore.path=/src/${SIGNER_KEYSTORE} \
    -Dstore.password=${SIGNER_STORE_PASS} \
    -Djarsigner.alias=${SIGNER_ALIAS} \
    -Dkey.password=${SIGNER_KEY_PASS} \
    -Dbuild.number=${BUILD_NUMBER}

#Clean up
RUN rm -f ${SIGNER_KEYSTORE}

# We specifically clean the source out since we expect downstream containers
# to copy in their own.
RUN rm -rf /src/*
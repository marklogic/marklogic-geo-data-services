FROM gradle:jdk12 AS build
#FROM store/oracle/serverjre:8 AS build
#FROM gradle:jdk8

# tools
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get install -y gcc && \
    apt-get install -y make

# pull from repo
RUN git clone https://github.com/marklogic-community/marklogic-geo-data-services /gds

# wait script
COPY wait-for-ml.sh /gds/wait-for-ml.sh
RUN chmod 777 /gds/wait-for-ml.sh

# replace build.gradle (w/ example data tasks)
COPY build.gradle /gds/examples/sample-project/build.gradle

# deploy
WORKDIR /gds/examples/sample-project
CMD /gds/wait-for-ml.sh $MLHOST 8001 $MLUSER $MLPWD && \
    gradle -PmlHost=ml mlDeploy && \
    gradle -PmlHost=ml downloadExampleData && \
    gradle -PmlHost=ml loadExampleData
    #tail -f /dev/null
    
FROM gradle:jdk12 AS build

# tools
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get install -y gcc && \
    apt-get install -y make

# copy project files
COPY examples/sample-project /gds

# wait script
COPY examples/sample-docker/deployer/wait-for-ml.sh /gds/wait-for-ml.sh
RUN chmod 777 /gds/wait-for-ml.sh

# Koop configuration
COPY examples/sample-docker/deployer/koop /gds/src/main/ml-data/koop

# deploy
WORKDIR /gds
CMD ./wait-for-ml.sh $MLHOST 8001 $MLUSER $MLPWD && \
    gradle -PmlHost=ml mlDeploy && \
    gradle -PmlHost=ml downloadExampleData && \
    gradle -PmlHost=ml loadExampleData
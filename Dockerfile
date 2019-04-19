# K.U.R.E.U.I.L
#
#
# version: '3'

FROM bigtruedata/sbt

# get kureuil-api
COPY . /kureuil-api
RUN chown root:root -R /kureuil-api


#install kureuil-api
RUN (cd /kureuil-api && sbt compile)

# start kureuil-api
CMD (cd /kureuil-api/ && sbt kureuil-api-run/run)

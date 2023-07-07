echo 'Starting building project, running tests and creating docker images, please be patient it will take a couple of minutes depending on your network connection....'
./mvnw clean package
echo 'Project build finished successfully!'
echo 'Starting services'
docker-compose up -d
echo 'Services deployed successfully, enjoy!'
echo 'To stop services run docker-compose down '








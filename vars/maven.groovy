def call(String commad){
    sh 'chmod +x mvnw'
    sh "./mvnw ${commad}"
}
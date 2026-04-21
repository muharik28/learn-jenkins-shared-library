def call(List commads){
    for (commad in commads) {
        sh 'chmod +x mvnw'
        sh "./mvnw ${commad}"
    }
}
From openjdk:15
copy ./target/currency-monitor-0.0.1-SNAPSHOT.jar currency-monitor-0.0.1-SNAPSHOT.jar
CMD ["java","-jar","currency-monitor-0.0.1-SNAPSHOT.jar"]
default: clean build # cleans then builds

clean: # clean files using gradle wrapper
	./gradlew clean

build: # build into an uberjar that includes deps
	./gradlew shadowJar

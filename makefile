default: clean build # cleans then builds

clean: # clean files using gradle wrapper
	./gradlew clean

build: # build into an uberjar that includes deps
	./gradlew shadowJar

deps: # list all dependencies that my project uses
	./gradlew dependencies

props: # list properties of the project
	./gradlew properties

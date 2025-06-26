FROM ubuntu:22.04

# Basic tools and dependencies
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk openjdk-17-jdk-headless \
    git cmake build-essential swig wget \
    libreadline-dev zlib1g-dev libboost-all-dev \
    libgmp-dev libmpfr-dev \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV SCIP_HOME=/opt/scipopt
ENV JSCIP_HOME=/opt/JSCIPOpt
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$SCIP_HOME:$JAVA_HOME/bin

# Create working directory
WORKDIR /opt

# Install SoPlex (use compatible version)
RUN git clone https://github.com/scipopt/soplex.git /opt/soplex && \
    cd /opt/soplex && \
    git checkout release-700 && \
    mkdir build && cd build && \
    cmake .. -DCMAKE_INSTALL_PREFIX=/opt/soplex/install && \
    make -j$(nproc) && make install

# Clone SCIP (use compatible version with JSCIPOpt)
RUN git clone https://github.com/scipopt/scip.git /opt/scipopt && \
    cd /opt/scipopt && \
    git checkout v804 && \
    git submodule update --init --recursive && \
    mkdir build && cd build && \
    cmake .. -DAUTOBUILD=ON -DSOPLEX_DIR=/opt/soplex/install -DSHARED=ON && \
    make -j$(nproc)

# Clone and build JSCIPOpt (use compatible version)
RUN git clone https://github.com/scipopt/JSCIPOpt.git /opt/JSCIPOpt && \
    cd /opt/JSCIPOpt && \
    mkdir build && cd build && \
    cmake .. -DSCIP_DIR=/opt/scipopt/build -DJAVA_HOME=$JAVA_HOME -DBUILD_JAVA=ON && \
    make -j$(nproc)


RUN apt-get update && apt-get install -y tree
RUN echo "📁 Full layout of /opt/JSCIPOpt/build" && tree /opt/JSCIPOpt/build

# Compile your Java code
#WORKDIR /app
#COPY src/ ./src/
#RUN mkdir -p build && \
#    javac -cp $JSCIP_HOME/build/Release/scip.jar -d build src/Main.java

# Run the Java program
#CMD ["java", "-cp", "/opt/JSCIPOpt/build/Release/scip.jar:/app/build", "-Djava.library.path=/opt/JSCIPOpt/build/Release", "Main"]

# Create app directory
WORKDIR /app
RUN mkdir -p build

# Default command that compiles and runs
CMD ["sh", "-c", "javac -cp $JSCIP_HOME/build/Release/scip.jar -d build src/*.java && java -cp $JSCIP_HOME/build/Release/scip.jar:/app/build -Djava.library.path=/opt/JSCIPOpt/build/Release Main"]

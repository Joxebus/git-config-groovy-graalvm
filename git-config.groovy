#!/usr/bin/env groovy

/**
* Installation:
* chmod +x git-config
* cp git-config /usr/local/bin
*/
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

String HOME_FOLDER = System.getProperty("user.home") + File.separator + "git-config"
// Read the input from console
String[] args = getProperty("args") as String[]

def usage = { ->
    println """
usage: git-config -[hvroi]
 -c,--configure     Configure user info
 -h,--help          Usage Information
 -p,--profile       Use profile configuration
 -s,--show          Show user info
 -sl,--show-local   Show local configuration
"""
}


if (!args || args.length > 2) {
    println "Invalid arguments, please see the usage section: "
    usage()
    return
}
File homeFolder = new File(HOME_FOLDER)
File configurationFile = new File(HOME_FOLDER, "config.json")

def showLocalConfiguration = { ->
    print "Repo:  "+('git config --get remote.origin.url'.execute().text)
    print "Name:  "+('git config --local user.name'.execute().text)
    print "Email: "+('git config --local user.email'.execute().text)
}

def readData = { type ->
    println "Setup [$type] data"
    Map config = [:]
    print "Name:  "
    config['userName'] = System.in.newReader().readLine()
    print "Email: "
    config['userEmail'] = System.in.newReader().readLine()
    println "-------------------"
    config
}

def readAndValidateProfile = {
    print "Do you want to add new profile? y/n: "
    String answer = System.in.newReader().readLine().toLowerCase()
    if( answer != 'y' ) {
        return false
    }

    print "Type the name of the profile: "
    answer = System.in.newReader().readLine().toLowerCase()
    answer
}

def setupRepositoryConfig = { profile, Map gitConfig ->
    Map config = gitConfig
    if(!config) {
       println "Invalid configuration [${profile}] please verify the information and try again"
       System.exit(1)
    }
    println "Setting up configuration [${profile}] for the current repository"
    "git config user.email ${config.userEmail}".execute().waitFor()
    ['git', 'config', 'user.name', config.userName].execute().waitFor()

}

def configure = {
    println "Setting up configuration"
    def profileName = 'personal'
    Map configuration = [:]

    do {
      configuration << [(profileName) : readData(profileName)]
    } while((profileName = readAndValidateProfile()))

    configurationFile.withWriter { out ->
        out.write(JsonOutput.prettyPrint(JsonOutput.toJson(configuration)))
        out.close()
    }
    configuration
}

def readConfiguration = {
    Map<String, Map> configuration = [:]
    try {
        configuration = (Map<String, Map>) new JsonSlurper().parseText(configurationFile.text)
        if(configuration.isEmpty()) {
            configuration = configure()
        }
    } catch (IllegalArgumentException ia) {
        println ia.message
        configuration = configure()
    }
    configuration
}

if(!homeFolder.exists()) {
    println "Creating home folder [${HOME_FOLDER}]"
    homeFolder.mkdirs()
}

if (["-h", "--help"].contains(args[0])) {
    usage()
    return
}

if(["-c", "--configure"].contains(args[0])) {
    configure()
    return
}


if(!configurationFile.exists()) {
    println "Configuration file doesn't exists, creating default configuration on ${HOME_FOLDER}"
    configurationFile = new File(HOME_FOLDER, "config.json")
    configurationFile.createNewFile()
}

if(["-s", "--show"].contains(args[0])) {
    println configurationFile.text
    return
}

if(["-sl", "--show-local"].contains(args[0])) {
    showLocalConfiguration()
    return
}

// Setup configuration for local env
Map configuration = readConfiguration()
if(["-p", "--profile"].contains(args[0])) {
    String profile = args[1]
    setupRepositoryConfig(profile, configuration[profile])
} else {
    setupRepositoryConfig('personal', configuration['personal'])
}
showLocalConfiguration()

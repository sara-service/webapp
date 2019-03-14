# How to Install Sara Server on bwCloud Scope

## Intro

This manual provides a step-by-step setup for a fully configured instance Sara server. 
It is advised to walk through this manual without interruptions or intermediate reboots.

About SARA:
https://sara-service.org

In case of questions please contact:
* Stefan Kombrink, Ulm University, Germany / e-mail: stefan.kombrink[at]uni-ulm.de
* Matthias Fratz, University of Constance / email: matthias.fratz[at]uni-konstanz.de
* Franziska Rapp, Ulm University, Germany / e-mail: franziska.rapp[at]uni-ulm.de

## Requirements

You will need
* a Source GitLab (or GitHub)
* a DSpace-Repository
* an Archive GitLab

## Setup 

### Create a virtual machine (e.g. an instance on the bwCloud):

  * https://portal.bw-cloud.org
  * Compute -> Instances -> Start new instance
  * Use "Ubuntu Server 18.04 Minimal" image
  * Use flavor "m1.medium" with 12GB disk space and 4GB RAM
  * Enable port 8080 egress/ingress by creating and enabling a new Security Group 'tomcat'
  * Enable port 80/443 egress/ingress by creating and enabling a new Security Group 'apache'

### In case you have an running instance already which you would like to replace

 * https://portal.bw-cloud.org
 * Compute -> Instances -> "sara-server" -> [Rebuild Instance]
 * You might need to remove your old SSH key from ~/.ssh/known_hosts
 
 ### Setup DNS
 * create a DNS record for your machine, here: `ulm.sara-service.org`

### Connect to the machine
```bash
ssh -A ubuntu@ulm.sara-service.org
```

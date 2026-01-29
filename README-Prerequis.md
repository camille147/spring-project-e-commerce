# Prérequis

Ce fichier décrit rapidement comment installer Docker / Docker Compose et Java 17 (JDK) sur Windows, macOS et Linux.

## Vérifications générales
- Vérifier les versions :
    - `docker --version`
    - `docker compose version`
    - `java -version`
    - `javac -version`

---

## Windows
1. Docker
    - Installer Docker Desktop depuis le site officiel : https://www.docker.com/products/docker-desktop
    - Activer WSL2 si demandé (recommandé pour Linux containers).
    - Lancer Docker Desktop et vérifier : `docker --version` / `docker compose version`.

2. Java 17 (JDK)
    - Installer via l'installateur officiel Temurin/Adoptium ou via `winget` / Chocolatey.
    - Exemple avec winget :
        - `winget install --id EclipseAdoptium.Temurin.17.JDK`
    - Définir `JAVA_HOME` si nécessaire (exemple PowerShell) :
        - `setx JAVA_HOME "C:\Program Files\AdoptOpenJDK\jdk-17"` (ajuster le chemin)
    - Vérifier : `java -version`

---

## macOS
1. Docker
    - Installer Docker Desktop pour Mac (Intel ou Apple Silicon) : https://www.docker.com/products/docker-desktop
    - Ou via Homebrew : `brew install --cask docker` puis ouvrir l'application Docker.
    - Vérifier : `docker --version` / `docker compose version`

2. Java 17 (JDK)
    - Via Homebrew :
        - `brew install openjdk@17`
        - Ajouter au shell (exemple pour Homebrew sur Intel) :
            - `echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc`
        - Pour Apple Silicon, ajuster le chemin `/opt/homebrew/opt/openjdk@17`.
    - Vérifier : `java -version`

---

## Linux (ex. Ubuntu/Debian)
1. Docker (méthode officielle résumée)
    - Installer prérequis :
        - `sudo apt update`
        - `sudo apt install ca-certificates curl gnupg lsb-release -y`
    - Ajouter la clé GPG et le dépôt Docker (suivre instructions officielles si besoin).
    - Installer Docker Engine et Compose plugin :
        - `sudo apt update`
        - `sudo apt install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y`
    - Autoriser l'utilisation sans `sudo` (re-login requis) :
        - `sudo usermod -aG docker $USER`
    - Vérifier : `docker --version` / `docker compose version`

2. Java 17 (JDK)
    - Installer via paquet officiel :
        - `sudo apt update`
        - `sudo apt install openjdk-17-jdk -y`
    - Définir `JAVA_HOME` (exemple) :
        - `echo "export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))" >> ~/.bashrc`
        - `source ~/.bashrc`
    - Vérifier : `java -version`

---

## Remarques
- Toujours préférer les pages officielles pour les téléchargements et instructions détaillées.
- Sur Linux, adapter les commandes selon la distribution (RHEL/CentOS utilisent `dnf`/`yum`).
- Pour CI ou serveurs, utiliser les packages `docker-ce` et le plugin `docker-compose` (Compose V2) ou `docker-compose` standalone si nécessaire.

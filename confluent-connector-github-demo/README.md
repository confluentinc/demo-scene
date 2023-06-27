# Confluent Connector Github Demo 

In this demo, you'll learn how to set up the Confluent GitHub Connector, and then how to generate your own data with commit events that surface to the Confluent interface! 

Let's get started. 

## Prerequisites

1. A [Confluent Cloud](https://rebrand.ly/f0kvol5) account. 
2. A [GitHub account](https://docs.github.com/en/get-started/signing-up-for-github/signing-up-for-a-new-github-account). You'll also need a [classic personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-personal-access-token-classic) for accessing the GitHub REST API. Use the 'repo' permission setting. To test your token, run this command in your terminal:

```
curl -L \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer <YOUR-TOKEN>"\
  -H "X-GitHub-Api-Version: 2022-11-28" \
  https://api.github.com/emojis
  ```
  You'll get a list of emojis returned if your GitHub token is working. 😊
  
  ## Step 1: Create a test repo.
  
  [Create a new GitHub repo](https://docs.github.com/en/get-started/quickstart/create-a-repo) with an easily editable file in it on the main branch, like a `README.md`. 
  
  ## Step 2: Configure your Confluent Cloud connector. 
 
 Log in to the Confluent Cloud interface. 
 
 Sign in to your Confluent Cloud account. Head over to the [confluent.cloud/environments](https://confluent.cloud/environments) page and click 'Add Cloud Environment' on the top right of your screen. 

<img width="1459" alt="click Add Cloud Environment on the top right" src="https://user-images.githubusercontent.com/54046179/220384774-b7518172-d674-4f92-ab80-6b4ac7aa6cf4.png">

Name your environment 'github_data' and click 'Create'. Note: If you're prompted to select a Stream Governance package, do it and accept the default free option. This will help enable the automatic schema registry that the GitHub connector will need to use. 

On your cluster page, click 'Create cluster on my own' or 'Create cluster'. 

In the navbar on the left, select 'Connectors'. Search the connectors for "GitHub". _There will be two results. Select the one with the GitHub logo present._ 

Generate the API key and download as prompted. You'll then be prompted to enter these two values:

<img width="885" alt="values for GitHub authentication" src="https://user-images.githubusercontent.com/54046179/222235211-b7862a81-0a00-4ab9-92ed-9cb83eb275b4.png">

For 'GitHub Endpoint', enter `https://api.github.com`.

For 'GitHub Access Token', enter the classic personal token you created earlier. You do not need to preface it with 'Bearer'. 

Next, add configuration details. Set the output record format to 'Avro'. 

<img width="955" alt="connection details" src="https://user-images.githubusercontent.com/54046179/222235864-4324240b-d82f-4ba1-adfd-e52f92b22573.png">

> Note: copy and paste this value into the 'Topic Name Pattern' field:  `github-${resourceName}` 

Under 'GitHub Repositories', enter your username/repo. If you enter the full url, the connector will fail. 

Under 'GitHub Resources', select 'commits'. 

Under 'Since', put the date you want to read commits from. It's important it be in the format 'YYYY-MM-DD', including the dashes. 

As far as the sizing goes, default values are ok. 

## Step 3: Generate an event! 

Now, head over to your repository on GitHub, add some text to your `README.md`, and commit. If you return to the Confluent Cloud interface, you'll see a commit message under the topics `github-commits`. Your commit has been produced and read as an event in Kafka! 

 <img width="993" alt="commit message" src="https://user-images.githubusercontent.com/54046179/222236919-27f5bb85-eae0-4fe1-a94b-63c3cb3d350e.png">

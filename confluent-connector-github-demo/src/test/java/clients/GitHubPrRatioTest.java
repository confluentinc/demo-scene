package clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.GitHubPRStateCounter;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Test;
import serde.StreamsSerde;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitHubPrRatioTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldProcessGitHubPRJson() throws Exception {
        final Serde<GitHubPRStateCounter> prStateCounterSerde = StreamsSerde.serdeFor(GitHubPRStateCounter.class);
        final Serializer<JsonNode> jsonSerializer = new JsonSerializer();

        Properties properties = new Properties();
        Topology topology = new GitHubPrRatio().topology(properties);

        try (TopologyTestDriver driver = new TopologyTestDriver(topology)) {
            TestInputTopic<String, JsonNode> inputTopic = driver.createInputTopic(GitHubPrRatio.INPUT_TOPIC, Serdes.String().serializer(), jsonSerializer);
            TestOutputTopic<String, GitHubPRStateCounter> outputTopic = driver.createOutputTopic(GitHubPrRatio.OUTPUT_TOPIC, Serdes.String().deserializer(), prStateCounterSerde.deserializer());

            JsonNode openPR = objectMapper.readTree(githubOpenPRString);
            JsonNode closedPR = objectMapper.readTree(githubClosedPRString);
            JsonNode noStatePR = objectMapper.readTree(githubPrStringNullState);

            Instant instant = Instant.now();
            inputTopic.pipeInput("pr", openPR, instant);

            instant = instant.plus(2, ChronoUnit.SECONDS);
            inputTopic.pipeInput("pr", closedPR, instant);

            instant = instant.plus(2, ChronoUnit.SECONDS);
            inputTopic.pipeInput("pr", noStatePR, instant);

            GitHubPRStateCounter expectedGHStateCounter = new GitHubPRStateCounter();
            expectedGHStateCounter.setOpen(1);
            expectedGHStateCounter.setClosed(1);
            Map<String, GitHubPRStateCounter> counterMap = outputTopic.readKeyValuesToMap();
            assertEquals(expectedGHStateCounter, counterMap.get("pr"));

        }
    }

    String githubOpenPRString = "{\n" +
            "  \"number\": 4,\n" +
            "  \"repository\": {\n" +
            "    \"events_url\": \"https://api.github.com/repos/naholyr/gith/events\",\n" +
            "    \"name\": \"gith\",\n" +
            "    \"assignees_url\": \"https://api.github.com/repos/naholyr/gith/assignees{/user}\",\n" +
            "    \"issue_comment_url\": \"https://api.github.com/repos/naholyr/gith/issues/comments/{number}\",\n" +
            "    \"git_tags_url\": \"https://api.github.com/repos/naholyr/gith/git/tags{/sha}\",\n" +
            "    \"open_issues_count\": 4,\n" +
            "    \"size\": 120,\n" +
            "    \"created_at\": \"2012-12-07T15:14:53Z\",\n" +
            "    \"issues_url\": \"https://api.github.com/repos/naholyr/gith/issues{/number}\",\n" +
            "    \"has_wiki\": true,\n" +
            "    \"forks_count\": 1,\n" +
            "    \"subscription_url\": \"https://api.github.com/repos/naholyr/gith/subscription\",\n" +
            "    \"tags_url\": \"https://api.github.com/repos/naholyr/gith/tags{/tag}\",\n" +
            "    \"branches_url\": \"https://api.github.com/repos/naholyr/gith/branches{/branch}\",\n" +
            "    \"merges_url\": \"https://api.github.com/repos/naholyr/gith/merges\",\n" +
            "    \"clone_url\": \"https://github.com/naholyr/gith.git\",\n" +
            "    \"hooks_url\": \"https://api.github.com/repos/naholyr/gith/hooks\",\n" +
            "    \"git_commits_url\": \"https://api.github.com/repos/naholyr/gith/git/commits{/sha}\",\n" +
            "    \"private\": false,\n" +
            "    \"forks_url\": \"https://api.github.com/repos/naholyr/gith/forks\",\n" +
            "    \"updated_at\": \"2012-12-07T15:32:18Z\",\n" +
            "    \"contents_url\": \"https://api.github.com/repos/naholyr/gith/contents/{+path}\",\n" +
            "    \"watchers\": 0,\n" +
            "    \"languages_url\": \"https://api.github.com/repos/naholyr/gith/languages\",\n" +
            "    \"ssh_url\": \"git@github.com:naholyr/gith.git\",\n" +
            "    \"fork\": true,\n" +
            "    \"keys_url\": \"https://api.github.com/repos/naholyr/gith/keys{/key_id}\",\n" +
            "    \"url\": \"https://api.github.com/repos/naholyr/gith\",\n" +
            "    \"issue_events_url\": \"https://api.github.com/repos/naholyr/gith/issues/events{/number}\",\n" +
            "    \"labels_url\": \"https://api.github.com/repos/naholyr/gith/labels{/name}\",\n" +
            "    \"git_url\": \"git://github.com/naholyr/gith.git\",\n" +
            "    \"notifications_url\": \"https://api.github.com/repos/naholyr/gith/notifications{?since,all,participating}\",\n" +
            "    \"language\": \"JavaScript\",\n" +
            "    \"id\": 7055082,\n" +
            "    \"svn_url\": \"https://github.com/naholyr/gith\",\n" +
            "    \"commits_url\": \"https://api.github.com/repos/naholyr/gith/commits{/sha}\",\n" +
            "    \"downloads_url\": \"https://api.github.com/repos/naholyr/gith/downloads\",\n" +
            "    \"pushed_at\": \"2012-12-07T15:30:45Z\",\n" +
            "    \"comments_url\": \"https://api.github.com/repos/naholyr/gith/comments{/number}\",\n" +
            "    \"open_issues\": 4,\n" +
            "    \"has_downloads\": true,\n" +
            "    \"mirror_url\": null,\n" +
            "    \"archive_url\": \"https://api.github.com/repos/naholyr/gith/{archive_format}{/ref}\",\n" +
            "    \"collaborators_url\": \"https://api.github.com/repos/naholyr/gith/collaborators{/collaborator}\",\n" +
            "    \"homepage\": null,\n" +
            "    \"statuses_url\": \"https://api.github.com/repos/naholyr/gith/statuses/{sha}\",\n" +
            "    \"has_issues\": false,\n" +
            "    \"trees_url\": \"https://api.github.com/repos/naholyr/gith/git/trees{/sha}\",\n" +
            "    \"full_name\": \"naholyr/gith\",\n" +
            "    \"git_refs_url\": \"https://api.github.com/repos/naholyr/gith/git/refs{/sha}\",\n" +
            "    \"description\": \"simple node server that responds to github post-receive events with meaningful data\",\n" +
            "    \"teams_url\": \"https://api.github.com/repos/naholyr/gith/teams\",\n" +
            "    \"forks\": 1,\n" +
            "    \"stargazers_url\": \"https://api.github.com/repos/naholyr/gith/stargazers\",\n" +
            "    \"subscribers_url\": \"https://api.github.com/repos/naholyr/gith/subscribers\",\n" +
            "    \"watchers_count\": 0,\n" +
            "    \"html_url\": \"https://github.com/naholyr/gith\",\n" +
            "    \"blobs_url\": \"https://api.github.com/repos/naholyr/gith/git/blobs{/sha}\",\n" +
            "    \"compare_url\": \"https://api.github.com/repos/naholyr/gith/compare/{base}...{head}\",\n" +
            "    \"milestones_url\": \"https://api.github.com/repos/naholyr/gith/milestones{/number}\",\n" +
            "    \"owner\": {\n" +
            "      \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "      \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "      \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "      \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "      \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "      \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "      \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "      \"id\": 214067,\n" +
            "      \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "      \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "      \"type\": \"User\",\n" +
            "      \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "      \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "      \"login\": \"naholyr\",\n" +
            "      \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "    },\n" +
            "    \"contributors_url\": \"https://api.github.com/repos/naholyr/gith/contributors\",\n" +
            "    \"pulls_url\": \"https://api.github.com/repos/naholyr/gith/pulls{/number}\"\n" +
            "  },\n" +
            "  \"pull_request\": {\n" +
            "    \"number\": 4,\n" +
            "    \"issue_url\": \"https://github.com/naholyr/gith/issues/4\",\n" +
            "    \"head\": {\n" +
            "      \"label\": \"bonjourmodule:test3\",\n" +
            "      \"repo\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"sha\": \"abd633ff2744039e4bda1c697cea3d03152df3fc\",\n" +
            "      \"ref\": \"test3\",\n" +
            "      \"user\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"merged_by\": null,\n" +
            "    \"created_at\": \"2012-12-07T15:50:13Z\",\n" +
            "    \"changed_files\": 1,\n" +
            "    \"merged\": false,\n" +
            "    \"comments\": 0,\n" +
            "    \"body\": \"\",\n" +
            "    \"title\": \"Test3\",\n" +
            "    \"additions\": 1,\n" +
            "    \"diff_url\": \"https://github.com/naholyr/gith/pull/4.diff\",\n" +
            "    \"updated_at\": \"2012-12-07T15:50:13Z\",\n" +
            "    \"url\": \"https://api.github.com/repos/naholyr/gith/pulls/4\",\n" +
            "    \"_links\": {\n" +
            "      \"html\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"self\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"comments\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"issue\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"review_comments\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"merge_commit_sha\": \"\",\n" +
            "    \"review_comments_url\": \"https://github.com/naholyr/gith/pull/4/comments\",\n" +
            "    \"id\": 3281103,\n" +
            "    \"commits_url\": \"https://github.com/naholyr/gith/pull/4/commits\",\n" +
            "    \"comments_url\": \"https://api.github.com/repos/naholyr/gith/issues/4/comments\",\n" +
            "    \"assignee\": null,\n" +
            "    \"patch_url\": \"https://github.com/naholyr/gith/pull/4.patch\",\n" +
            "    \"mergeable_state\": \"unknown\",\n" +
            "    \"mergeable\": null,\n" +
            "    \"milestone\": null,\n" +
            "    \"closed_at\": null,\n" +
            "    \"merged_at\": null,\n" +
            "    \"commits\": 2,\n" +
            "    \"user\": {\n" +
            "      \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "      \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "      \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "      \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "      \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "      \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "      \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "      \"id\": 214067,\n" +
            "      \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "      \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "      \"type\": \"User\",\n" +
            "      \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "      \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "      \"login\": \"naholyr\",\n" +
            "      \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "    },\n" +
            "    \"deletions\": 135,\n" +
            "    \"review_comments\": 0,\n" +
            "    \"html_url\": \"https://github.com/naholyr/gith/pull/4\",\n" +
            "    \"state\": \"open\",\n" +
            "    \"base\": {\n" +
            "      \"label\": \"naholyr:master\",\n" +
            "      \"repo\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"sha\": \"b493393b12a780735832a8ece5e16e723aef1318\",\n" +
            "      \"ref\": \"master\",\n" +
            "      \"user\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"review_comment_url\": \"/repos/naholyr/gith/pulls/comments/{number}\"\n" +
            "  },\n" +
            "  \"sender\": {\n" +
            "    \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "    \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "    \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "    \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "    \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "    \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "    \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "    \"id\": 214067,\n" +
            "    \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "    \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "    \"type\": \"User\",\n" +
            "    \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "    \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "    \"login\": \"naholyr\",\n" +
            "    \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "  },\n" +
            "  \"action\": \"opened\"\n" +
            "}";

    String githubClosedPRString = "{\n" +
            "  \"number\": 4,\n" +
            "  \"repository\": {\n" +
            "    \"events_url\": \"https://api.github.com/repos/naholyr/gith/events\",\n" +
            "    \"name\": \"gith\",\n" +
            "    \"assignees_url\": \"https://api.github.com/repos/naholyr/gith/assignees{/user}\",\n" +
            "    \"issue_comment_url\": \"https://api.github.com/repos/naholyr/gith/issues/comments/{number}\",\n" +
            "    \"git_tags_url\": \"https://api.github.com/repos/naholyr/gith/git/tags{/sha}\",\n" +
            "    \"open_issues_count\": 4,\n" +
            "    \"size\": 120,\n" +
            "    \"created_at\": \"2012-12-07T15:14:53Z\",\n" +
            "    \"issues_url\": \"https://api.github.com/repos/naholyr/gith/issues{/number}\",\n" +
            "    \"has_wiki\": true,\n" +
            "    \"forks_count\": 1,\n" +
            "    \"subscription_url\": \"https://api.github.com/repos/naholyr/gith/subscription\",\n" +
            "    \"tags_url\": \"https://api.github.com/repos/naholyr/gith/tags{/tag}\",\n" +
            "    \"branches_url\": \"https://api.github.com/repos/naholyr/gith/branches{/branch}\",\n" +
            "    \"merges_url\": \"https://api.github.com/repos/naholyr/gith/merges\",\n" +
            "    \"clone_url\": \"https://github.com/naholyr/gith.git\",\n" +
            "    \"hooks_url\": \"https://api.github.com/repos/naholyr/gith/hooks\",\n" +
            "    \"git_commits_url\": \"https://api.github.com/repos/naholyr/gith/git/commits{/sha}\",\n" +
            "    \"private\": false,\n" +
            "    \"forks_url\": \"https://api.github.com/repos/naholyr/gith/forks\",\n" +
            "    \"updated_at\": \"2012-12-07T15:32:18Z\",\n" +
            "    \"contents_url\": \"https://api.github.com/repos/naholyr/gith/contents/{+path}\",\n" +
            "    \"watchers\": 0,\n" +
            "    \"languages_url\": \"https://api.github.com/repos/naholyr/gith/languages\",\n" +
            "    \"ssh_url\": \"git@github.com:naholyr/gith.git\",\n" +
            "    \"fork\": true,\n" +
            "    \"keys_url\": \"https://api.github.com/repos/naholyr/gith/keys{/key_id}\",\n" +
            "    \"url\": \"https://api.github.com/repos/naholyr/gith\",\n" +
            "    \"issue_events_url\": \"https://api.github.com/repos/naholyr/gith/issues/events{/number}\",\n" +
            "    \"labels_url\": \"https://api.github.com/repos/naholyr/gith/labels{/name}\",\n" +
            "    \"git_url\": \"git://github.com/naholyr/gith.git\",\n" +
            "    \"notifications_url\": \"https://api.github.com/repos/naholyr/gith/notifications{?since,all,participating}\",\n" +
            "    \"language\": \"JavaScript\",\n" +
            "    \"id\": 7055082,\n" +
            "    \"svn_url\": \"https://github.com/naholyr/gith\",\n" +
            "    \"commits_url\": \"https://api.github.com/repos/naholyr/gith/commits{/sha}\",\n" +
            "    \"downloads_url\": \"https://api.github.com/repos/naholyr/gith/downloads\",\n" +
            "    \"pushed_at\": \"2012-12-07T15:30:45Z\",\n" +
            "    \"comments_url\": \"https://api.github.com/repos/naholyr/gith/comments{/number}\",\n" +
            "    \"open_issues\": 4,\n" +
            "    \"has_downloads\": true,\n" +
            "    \"mirror_url\": null,\n" +
            "    \"archive_url\": \"https://api.github.com/repos/naholyr/gith/{archive_format}{/ref}\",\n" +
            "    \"collaborators_url\": \"https://api.github.com/repos/naholyr/gith/collaborators{/collaborator}\",\n" +
            "    \"homepage\": null,\n" +
            "    \"statuses_url\": \"https://api.github.com/repos/naholyr/gith/statuses/{sha}\",\n" +
            "    \"has_issues\": false,\n" +
            "    \"trees_url\": \"https://api.github.com/repos/naholyr/gith/git/trees{/sha}\",\n" +
            "    \"full_name\": \"naholyr/gith\",\n" +
            "    \"git_refs_url\": \"https://api.github.com/repos/naholyr/gith/git/refs{/sha}\",\n" +
            "    \"description\": \"simple node server that responds to github post-receive events with meaningful data\",\n" +
            "    \"teams_url\": \"https://api.github.com/repos/naholyr/gith/teams\",\n" +
            "    \"forks\": 1,\n" +
            "    \"stargazers_url\": \"https://api.github.com/repos/naholyr/gith/stargazers\",\n" +
            "    \"subscribers_url\": \"https://api.github.com/repos/naholyr/gith/subscribers\",\n" +
            "    \"watchers_count\": 0,\n" +
            "    \"html_url\": \"https://github.com/naholyr/gith\",\n" +
            "    \"blobs_url\": \"https://api.github.com/repos/naholyr/gith/git/blobs{/sha}\",\n" +
            "    \"compare_url\": \"https://api.github.com/repos/naholyr/gith/compare/{base}...{head}\",\n" +
            "    \"milestones_url\": \"https://api.github.com/repos/naholyr/gith/milestones{/number}\",\n" +
            "    \"owner\": {\n" +
            "      \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "      \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "      \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "      \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "      \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "      \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "      \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "      \"id\": 214067,\n" +
            "      \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "      \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "      \"type\": \"User\",\n" +
            "      \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "      \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "      \"login\": \"naholyr\",\n" +
            "      \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "    },\n" +
            "    \"contributors_url\": \"https://api.github.com/repos/naholyr/gith/contributors\",\n" +
            "    \"pulls_url\": \"https://api.github.com/repos/naholyr/gith/pulls{/number}\"\n" +
            "  },\n" +
            "  \"pull_request\": {\n" +
            "    \"number\": 4,\n" +
            "    \"issue_url\": \"https://github.com/naholyr/gith/issues/4\",\n" +
            "    \"head\": {\n" +
            "      \"label\": \"bonjourmodule:test3\",\n" +
            "      \"repo\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"sha\": \"abd633ff2744039e4bda1c697cea3d03152df3fc\",\n" +
            "      \"ref\": \"test3\",\n" +
            "      \"user\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"merged_by\": null,\n" +
            "    \"created_at\": \"2012-12-07T15:50:13Z\",\n" +
            "    \"changed_files\": 1,\n" +
            "    \"merged\": false,\n" +
            "    \"comments\": 0,\n" +
            "    \"body\": \"\",\n" +
            "    \"title\": \"Test3\",\n" +
            "    \"additions\": 1,\n" +
            "    \"diff_url\": \"https://github.com/naholyr/gith/pull/4.diff\",\n" +
            "    \"updated_at\": \"2012-12-07T15:50:13Z\",\n" +
            "    \"url\": \"https://api.github.com/repos/naholyr/gith/pulls/4\",\n" +
            "    \"_links\": {\n" +
            "      \"html\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"self\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"comments\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"issue\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"review_comments\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"merge_commit_sha\": \"\",\n" +
            "    \"review_comments_url\": \"https://github.com/naholyr/gith/pull/4/comments\",\n" +
            "    \"id\": 3281103,\n" +
            "    \"commits_url\": \"https://github.com/naholyr/gith/pull/4/commits\",\n" +
            "    \"comments_url\": \"https://api.github.com/repos/naholyr/gith/issues/4/comments\",\n" +
            "    \"assignee\": null,\n" +
            "    \"patch_url\": \"https://github.com/naholyr/gith/pull/4.patch\",\n" +
            "    \"mergeable_state\": \"unknown\",\n" +
            "    \"mergeable\": null,\n" +
            "    \"milestone\": null,\n" +
            "    \"closed_at\": null,\n" +
            "    \"merged_at\": null,\n" +
            "    \"commits\": 2,\n" +
            "    \"user\": {\n" +
            "      \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "      \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "      \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "      \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "      \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "      \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "      \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "      \"id\": 214067,\n" +
            "      \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "      \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "      \"type\": \"User\",\n" +
            "      \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "      \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "      \"login\": \"naholyr\",\n" +
            "      \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "    },\n" +
            "    \"deletions\": 135,\n" +
            "    \"review_comments\": 0,\n" +
            "    \"html_url\": \"https://github.com/naholyr/gith/pull/4\",\n" +
            "    \"state\": \"closed\",\n" +
            "    \"base\": {\n" +
            "      \"label\": \"naholyr:master\",\n" +
            "      \"repo\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"sha\": \"b493393b12a780735832a8ece5e16e723aef1318\",\n" +
            "      \"ref\": \"master\",\n" +
            "      \"user\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"review_comment_url\": \"/repos/naholyr/gith/pulls/comments/{number}\"\n" +
            "  },\n" +
            "  \"sender\": {\n" +
            "    \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "    \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "    \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "    \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "    \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "    \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "    \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "    \"id\": 214067,\n" +
            "    \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "    \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "    \"type\": \"User\",\n" +
            "    \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "    \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "    \"login\": \"naholyr\",\n" +
            "    \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "  },\n" +
            "  \"action\": \"opened\"\n" +
            "}";

    String githubPrStringNullState = "{\n" +
            "  \"number\": 4,\n" +
            "  \"repository\": {\n" +
            "    \"events_url\": \"https://api.github.com/repos/naholyr/gith/events\",\n" +
            "    \"name\": \"gith\",\n" +
            "    \"assignees_url\": \"https://api.github.com/repos/naholyr/gith/assignees{/user}\",\n" +
            "    \"issue_comment_url\": \"https://api.github.com/repos/naholyr/gith/issues/comments/{number}\",\n" +
            "    \"git_tags_url\": \"https://api.github.com/repos/naholyr/gith/git/tags{/sha}\",\n" +
            "    \"open_issues_count\": 4,\n" +
            "    \"size\": 120,\n" +
            "    \"created_at\": \"2012-12-07T15:14:53Z\",\n" +
            "    \"issues_url\": \"https://api.github.com/repos/naholyr/gith/issues{/number}\",\n" +
            "    \"has_wiki\": true,\n" +
            "    \"forks_count\": 1,\n" +
            "    \"subscription_url\": \"https://api.github.com/repos/naholyr/gith/subscription\",\n" +
            "    \"tags_url\": \"https://api.github.com/repos/naholyr/gith/tags{/tag}\",\n" +
            "    \"branches_url\": \"https://api.github.com/repos/naholyr/gith/branches{/branch}\",\n" +
            "    \"merges_url\": \"https://api.github.com/repos/naholyr/gith/merges\",\n" +
            "    \"clone_url\": \"https://github.com/naholyr/gith.git\",\n" +
            "    \"hooks_url\": \"https://api.github.com/repos/naholyr/gith/hooks\",\n" +
            "    \"git_commits_url\": \"https://api.github.com/repos/naholyr/gith/git/commits{/sha}\",\n" +
            "    \"private\": false,\n" +
            "    \"forks_url\": \"https://api.github.com/repos/naholyr/gith/forks\",\n" +
            "    \"updated_at\": \"2012-12-07T15:32:18Z\",\n" +
            "    \"contents_url\": \"https://api.github.com/repos/naholyr/gith/contents/{+path}\",\n" +
            "    \"watchers\": 0,\n" +
            "    \"languages_url\": \"https://api.github.com/repos/naholyr/gith/languages\",\n" +
            "    \"ssh_url\": \"git@github.com:naholyr/gith.git\",\n" +
            "    \"fork\": true,\n" +
            "    \"keys_url\": \"https://api.github.com/repos/naholyr/gith/keys{/key_id}\",\n" +
            "    \"url\": \"https://api.github.com/repos/naholyr/gith\",\n" +
            "    \"issue_events_url\": \"https://api.github.com/repos/naholyr/gith/issues/events{/number}\",\n" +
            "    \"labels_url\": \"https://api.github.com/repos/naholyr/gith/labels{/name}\",\n" +
            "    \"git_url\": \"git://github.com/naholyr/gith.git\",\n" +
            "    \"notifications_url\": \"https://api.github.com/repos/naholyr/gith/notifications{?since,all,participating}\",\n" +
            "    \"language\": \"JavaScript\",\n" +
            "    \"id\": 7055082,\n" +
            "    \"svn_url\": \"https://github.com/naholyr/gith\",\n" +
            "    \"commits_url\": \"https://api.github.com/repos/naholyr/gith/commits{/sha}\",\n" +
            "    \"downloads_url\": \"https://api.github.com/repos/naholyr/gith/downloads\",\n" +
            "    \"pushed_at\": \"2012-12-07T15:30:45Z\",\n" +
            "    \"comments_url\": \"https://api.github.com/repos/naholyr/gith/comments{/number}\",\n" +
            "    \"open_issues\": 4,\n" +
            "    \"has_downloads\": true,\n" +
            "    \"mirror_url\": null,\n" +
            "    \"archive_url\": \"https://api.github.com/repos/naholyr/gith/{archive_format}{/ref}\",\n" +
            "    \"collaborators_url\": \"https://api.github.com/repos/naholyr/gith/collaborators{/collaborator}\",\n" +
            "    \"homepage\": null,\n" +
            "    \"statuses_url\": \"https://api.github.com/repos/naholyr/gith/statuses/{sha}\",\n" +
            "    \"has_issues\": false,\n" +
            "    \"trees_url\": \"https://api.github.com/repos/naholyr/gith/git/trees{/sha}\",\n" +
            "    \"full_name\": \"naholyr/gith\",\n" +
            "    \"git_refs_url\": \"https://api.github.com/repos/naholyr/gith/git/refs{/sha}\",\n" +
            "    \"description\": \"simple node server that responds to github post-receive events with meaningful data\",\n" +
            "    \"teams_url\": \"https://api.github.com/repos/naholyr/gith/teams\",\n" +
            "    \"forks\": 1,\n" +
            "    \"stargazers_url\": \"https://api.github.com/repos/naholyr/gith/stargazers\",\n" +
            "    \"subscribers_url\": \"https://api.github.com/repos/naholyr/gith/subscribers\",\n" +
            "    \"watchers_count\": 0,\n" +
            "    \"html_url\": \"https://github.com/naholyr/gith\",\n" +
            "    \"blobs_url\": \"https://api.github.com/repos/naholyr/gith/git/blobs{/sha}\",\n" +
            "    \"compare_url\": \"https://api.github.com/repos/naholyr/gith/compare/{base}...{head}\",\n" +
            "    \"milestones_url\": \"https://api.github.com/repos/naholyr/gith/milestones{/number}\",\n" +
            "    \"owner\": {\n" +
            "      \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "      \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "      \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "      \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "      \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "      \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "      \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "      \"id\": 214067,\n" +
            "      \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "      \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "      \"type\": \"User\",\n" +
            "      \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "      \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "      \"login\": \"naholyr\",\n" +
            "      \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "    },\n" +
            "    \"contributors_url\": \"https://api.github.com/repos/naholyr/gith/contributors\",\n" +
            "    \"pulls_url\": \"https://api.github.com/repos/naholyr/gith/pulls{/number}\"\n" +
            "  },\n" +
            "  \"pull_request\": {\n" +
            "    \"number\": 4,\n" +
            "    \"issue_url\": \"https://github.com/naholyr/gith/issues/4\",\n" +
            "    \"head\": {\n" +
            "      \"label\": \"bonjourmodule:test3\",\n" +
            "      \"repo\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"sha\": \"abd633ff2744039e4bda1c697cea3d03152df3fc\",\n" +
            "      \"ref\": \"test3\",\n" +
            "      \"user\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"merged_by\": null,\n" +
            "    \"created_at\": \"2012-12-07T15:50:13Z\",\n" +
            "    \"changed_files\": 1,\n" +
            "    \"merged\": false,\n" +
            "    \"comments\": 0,\n" +
            "    \"body\": \"\",\n" +
            "    \"title\": \"Test3\",\n" +
            "    \"additions\": 1,\n" +
            "    \"diff_url\": \"https://github.com/naholyr/gith/pull/4.diff\",\n" +
            "    \"updated_at\": \"2012-12-07T15:50:13Z\",\n" +
            "    \"url\": \"https://api.github.com/repos/naholyr/gith/pulls/4\",\n" +
            "    \"_links\": {\n" +
            "      \"html\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"self\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"comments\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"issue\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"review_comments\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"merge_commit_sha\": \"\",\n" +
            "    \"review_comments_url\": \"https://github.com/naholyr/gith/pull/4/comments\",\n" +
            "    \"id\": 3281103,\n" +
            "    \"commits_url\": \"https://github.com/naholyr/gith/pull/4/commits\",\n" +
            "    \"comments_url\": \"https://api.github.com/repos/naholyr/gith/issues/4/comments\",\n" +
            "    \"assignee\": null,\n" +
            "    \"patch_url\": \"https://github.com/naholyr/gith/pull/4.patch\",\n" +
            "    \"mergeable_state\": \"unknown\",\n" +
            "    \"mergeable\": null,\n" +
            "    \"milestone\": null,\n" +
            "    \"closed_at\": null,\n" +
            "    \"merged_at\": null,\n" +
            "    \"commits\": 2,\n" +
            "    \"user\": {\n" +
            "      \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "      \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "      \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "      \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "      \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "      \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "      \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "      \"id\": 214067,\n" +
            "      \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "      \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "      \"type\": \"User\",\n" +
            "      \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "      \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "      \"login\": \"naholyr\",\n" +
            "      \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "    },\n" +
            "    \"deletions\": 135,\n" +
            "    \"review_comments\": 0,\n" +
            "    \"html_url\": \"https://github.com/naholyr/gith/pull/4\",\n" +
            "    \"base\": {\n" +
            "      \"label\": \"naholyr:master\",\n" +
            "      \"repo\": [\n" +
            "        null\n" +
            "      ],\n" +
            "      \"sha\": \"b493393b12a780735832a8ece5e16e723aef1318\",\n" +
            "      \"ref\": \"master\",\n" +
            "      \"user\": [\n" +
            "        null\n" +
            "      ]\n" +
            "    },\n" +
            "    \"review_comment_url\": \"/repos/naholyr/gith/pulls/comments/{number}\"\n" +
            "  },\n" +
            "  \"sender\": {\n" +
            "    \"avatar_url\": \"https://secure.gravatar.com/avatar/396445b6e14727dcb6b3d6a66d52b567?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png\",\n" +
            "    \"events_url\": \"https://api.github.com/users/naholyr/events{/privacy}\",\n" +
            "    \"gists_url\": \"https://api.github.com/users/naholyr/gists{/gist_id}\",\n" +
            "    \"gravatar_id\": \"396445b6e14727dcb6b3d6a66d52b567\",\n" +
            "    \"received_events_url\": \"https://api.github.com/users/naholyr/received_events\",\n" +
            "    \"starred_url\": \"https://api.github.com/users/naholyr/starred{/owner}{/repo}\",\n" +
            "    \"url\": \"https://api.github.com/users/naholyr\",\n" +
            "    \"id\": 214067,\n" +
            "    \"following_url\": \"https://api.github.com/users/naholyr/following\",\n" +
            "    \"subscriptions_url\": \"https://api.github.com/users/naholyr/subscriptions\",\n" +
            "    \"type\": \"User\",\n" +
            "    \"organizations_url\": \"https://api.github.com/users/naholyr/orgs\",\n" +
            "    \"repos_url\": \"https://api.github.com/users/naholyr/repos\",\n" +
            "    \"login\": \"naholyr\",\n" +
            "    \"followers_url\": \"https://api.github.com/users/naholyr/followers\"\n" +
            "  },\n" +
            "  \"action\": \"opened\"\n" +
            "}";


}
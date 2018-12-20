import sys
import requests

APOLLO_UNDEPLOYED_BASE_URL = "http://apollo.internal.logz.io/api/status/get-undeployed-services"

try:
    import slackweb

except ImportError:
    print("Could not import slackweb..")
    print("Try: pip install slackweb")
    slackweb = None
    sys.exit(1)

SLACK_CHANNELS = ["#api-tests"]
SLACK_URL = "https://hooks.slack.com/services/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"
DAYS_UNDEPLOYED = 1


def notify_in_slack(apollo_response):

    fields_array = []
    for environment in apollo_response:
        service_list = []
        for service in environment["serviceGroupMap"]:
            if len(environment["serviceGroupMap"][service]) == 0:
                service_list.append(service)
            else:  # There is a group..
                text = "{} ({})".format(service, ", ".join(environment["serviceGroupMap"][service]))
                service_list.append(text)

        fields_array.append({
            "title": environment["environmentName"],
            "value": ", ".join(service_list),
            "short": False
        })

    attachment = {
        "fallback": "Found services that were not deployed for more than {} days".format(str(DAYS_UNDEPLOYED)),
        "pretext": "Apollo Undeployed Alert",
        "text": "These services were not deployed for more than {} days!".format(str(DAYS_UNDEPLOYED)),
        "color": "warning",
        "fields": fields_array
    }

    slack = slackweb.Slack(url=SLACK_URL)

    for channel in SLACK_CHANNELS:
        slack.notify(username="Apollo", icon_url="https://i.imgur.com/FMVZ9id.png",
                     channel=channel, attachments=[attachment])


def get_apollo_undeployed_services(avaiablity, timeunit, duration):
    try:
        response = requests.get("{}/avaiability/{}/time-unit/{}/duration/{}"
                                .format(APOLLO_UNDEPLOYED_BASE_URL, avaiablity, timeunit, duration))
        if response.status_code != 200:
            print("Could not get undeployed services from apollo, got: (HTTP: {})".format(response.status_code))
            print(response.text)
            sys.exit(3)

        return response.json()

    except Exception as e:
        print("Excption while getting undeployed services from Apollo, got: {}".format(e))


def main():
    apollo_response = get_apollo_undeployed_services("STAGING", "DAYS", "1")
    notify_in_slack(apollo_response)


main()
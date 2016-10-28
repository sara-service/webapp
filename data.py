# General data-structures
#
# See here an examle:
# https://github.com/carthage-college/django-djpsilobus/blob/master/djpsilobus/core/data.py

# Metadata for creating a new item in a collection
METADATA_DEFAULT = {
    "default": [
        {
            "key": "dc.contributor.author",
            "language": None,
            "value": ""
        },
        {
            "key": "dc.title",
            "language": None,
            "value": ""
        },
        {
            "key": "dc.publisher",
            "language": None,
            "value": ""
        },
        {
            "key": "dc.date.issued",
            "language": None,
            "value": ""
        },
        {
            "key": "uulm.typeDCMI",
            "language": None,
            "value": "Software"
        },
        {
            "key": "dc.type",
            "language": None,
            "value": "Software"
        },
        {
            "key": "dc.description",
            "language": "en_US",
            "value": "sara-test"
        }
    ]
}

# Example from:
# https://github.com/carthage-college/django-djpsilobus/blob/master/djpsilobus/core/data.py
ITEM_METADATA = {
    "metadata":[
        {
            "key": "dc.contributor.author",
            "value": ""
        },
        {
            "key": "dc.description",
            "language": "en_US",
            "value": ""
        },
        {
            "key": "dc.title",
            "language": "en_US",
            "value": ""
        },
        {
            "key": "dc.title.alternative",
            "language": "en_US",
            "value": ""
        },
        {
            "key": "dc.subject",
            "language": "en_US",
            "value": ""
        },
        {
            "key": "dc.subject",
            "language": "en_US",
            "value": ""
        }
    ]
}


METADATA_ENTRY = {
    "key": "dc.description.abstract",
    "value": "This is the description abstract",
    "language": ""
}



METADATA_EXAMPLE_FULL = {
"metadata": [
    {'key': 'dc.contributor.author',
     'language': '',
     'value': 'Süslü, Mustafa Kemal'
    },
    {'key': 'dc.date.accessioned',
     'language': None,
     'value': '2016-01-11T14:22:28Z'},
    {'key': 'dc.date.available',
     'language': None,
     'value': '2016-01-11T14:22:28Z'},
    {'key': 'dc.date.created', 'language': '', 'value': '2006'},
    {'key': 'dc.description.abstract',
     'language': '',
     'value': 'In dieser Arbeit werden weltweit wichtige Aktienkursindizes (AI´s) '
              'dargestellt, wobei eine axiomatische Untersuchung im Vordergrund '
              'steht. Von besonderer Bedeutung ist hierbei die Struktur, d.h. '
              'auch insbesondere die verwendeten Indexformeln und die '
              'eingesetzten Korrekturmethoden für den Fall der Dividendenzahlung, '
              'Kapitalerhöhung und Indexrevision. Aufbauend auf diesen '
              'Ergebnissen bzw. Verbesserungsvorschlägen können sinnvolle '
              'Anforderungen in Form von aktienmarktspezifischen Axiomen '
              'aufgestellt werden, die zur Überprüfung von AI´s aus der Praxis '
              'genutzt werden.'},
    {'key': 'dc.language.iso', 'language': '', 'value': 'de'},
    {'key': 'dc.publisher', 'language': None, 'value': 'Universität Ulm'},
    {'key': 'dc.rights',
     'language': '',
     'value': 'Standard (Fassung vom 03.05.2003)'},
    {'key': 'dc.rights.uri',
     'language': '',
     'value': 'https://oparu.uni-ulm.de/xmlui/license_v1'},
    {'key': 'dc.subject.ddc', 'language': None, 'value': 'DDC 330 / Economics'},
    {'key': 'dc.subject.lcsh', 'language': '', 'value': 'Stock price indexes'},
    {'key': 'dc.title',
     'language': '',
     'value': 'Zur axiomatischen Fundierung und Struktur von Aktienkursindizes'},
    {'key': 'dc.type', 'language': '', 'value': 'Dissertation'},
    {'key': 'uulm.affiliationGeneral',
     'language': '',
     'value': 'Fakultät für Mathematik und Wirtschaftswissenschaften'},
    {'key': 'uulm.freischaltungVTS',
     'language': '',
     'value': '2006-12-21T12:21:48Z'},
    {'key': 'uulm.peerReview', 'language': '', 'value': 'nein'},
    {'key': 'uulm.shelfmark',
     'language': '',
     'value': 'Z: J-H 11.350 ; N: J-H 5.159'},
    {'key': 'uulm.typeDCMI', 'language': '', 'value': 'Text'},
    {'key': 'uulm.vtsID', 'language': '', 'value': '5795'},
    {'key': 'dc.identifier.doi',
     'language': None,
     'value': 'http://dx.doi.org/10.5072/OPARUtest-6003'},
    {'key': 'dc.identifier.ppn', 'language': None, 'value': '208853650'},
    {'key': 'dc.identifier.urn',
     'language': '',
     'value': 'http://nbn-resolving.de/urn:nbn:de:bsz:289-vts-57957'},
     {'key': 'dc.subject.gnd', 'language': '', 'value': 'Aktienindex'},
     {'key': 'dc.subject.gnd', 'language': '', 'value': 'Axiomatik'},
     {'key': 'uulm.category', 'language': None, 'value': 'Publikationen'}    
    ]
    }




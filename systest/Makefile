# Copyright 2017 Sandvine
# All Rights Reserved.
# 
#    Licensed under the Apache License, Version 2.0 (the "License"); you may
#    not use this file except in compliance with the License. You may obtain
#    a copy of the License at
# 
#         http://www.apache.org/licenses/LICENSE-2.0
# 
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#    License for the specific language governing permissions and limitations
#    under the License.

#
# These variables need to be defined in environment or passed in
# the make invocation.
# eg. 
#    export OSM_HOSTNAME=1.2.3.4:8008
#    export OS_AUTH_URL=https://<keystoneserver>:5000/v2.0
#    export OS_USERNAME=admin
#    export OS_PASSWORD=admin
#    export OS_PROJECT_NAME=admin
OSM_HOSTNAME ?=
OS_AUTH_URL ?=
OS_USERNAME ?=
OS_PASSWORD_NAME ?=
OS_PROJECT_NAME ?=

ifdef OS_AUTH_URL
    OPTION_OS_AUTH_URL=--os-url $(OS_AUTH_URL)
endif
ifdef OS_USERNAME
    OPTION_OS_USERNAME=--os-username $(OS_USERNAME)
endif
ifdef OS_PASSWORD
    OPTION_OS_PASSWORD=--os-password $(OS_PASSWORD)
endif
ifdef OS_PROJECT_NAME
    OPTION_OS_PROJECT_NAME=--os-project-name $(OS_PROJECT_NAME)
endif

ifdef TEST_VNFD_DESCRIPTORS
    OPTION_TEST_VNFD_DESCRIPTORS=--osm-vnfd-descriptor-packages $(TEST_VNFD_DESCRIPTORS)
endif
ifdef TEST_NSD_DESCRIPTORS
    OPTION_TEST_NSD_DESCRIPTORS=--osm-nsd-descriptor-packages $(TEST_NSD_DESCRIPTORS)
endif

DESCRIPTOR_REPO_NAME = descriptor-packages
DESCRIPTOR_REPO_DIR ?= $(shell pwd)/descriptor-packages
DESCRIPTOR_BUILD_DIR := $(DESCRIPTOR_REPO_DIR)/build
OPTION_DESCRIPTOR_BUILD_DIR=--osm-descriptor-packages $(DESCRIPTOR_BUILD_DIR)

TEST_OSM_NS_NAME_PREFIX=pytest-$(shell date +%D-%T)-
OPTION_TEST_OSM_NS_NAME_PREFIX=--osm-ns-name-prefix $(TEST_OSM_NS_NAME_PREFIX)

JUNITXML_DIR = reports

JUNITXML ?= pytest-output.xml

PYTEST_OPTIONS=
Q=@

DESCRIPTOR_REPO ?= https://osm.etsi.org/gerrit/osm/$(DESCRIPTOR_REPO_NAME)


TEST_VNFD_DESCRIPTORS ?= None
TEST_NSD_DESCRIPTORS  ?= None

.NOTPARALLEL:
all: smoke cirros ping_pong

define check_env_var
	$(Q)if [ -z "$($(1))" ]; then echo "error: $(1) not set"; exit 1; fi
endef

check_OSM_HOSTNAME:
	$(call check_env_var,OSM_HOSTNAME)

check_openstack_env:
	$(call check_env_var,OS_AUTH_URL)
	$(call check_env_var,OS_USERNAME)
	$(call check_env_var,OS_PASSWORD)
	$(call check_env_var,OS_PROJECT_NAME)

.PHONY: check_openstack_env check_OSM_HOSTNAME

descriptors:
	test -e $(DESCRIPTOR_REPO_NAME) || git clone $(DESCRIPTOR_REPO)
	$(MAKE) -C $(DESCRIPTOR_REPO_NAME)

report_dir:
	@mkdir -p reports

_run_test: report_dir
	$(Q)py.test \
        --osmhost $(OSM_HOSTNAME) \
        $(OPTION_OS_AUTH_URL) \
        $(OPTION_OS_USERNAME) \
        $(OPTION_OS_PASSWORD) \
        $(OPTION_OS_PROJECT_NAME) \
        $(OPTION_TEST_VNFD_DESCRIPTORS) \
        $(OPTION_TEST_NSD_DESCRIPTORS) \
        $(OPTION_DESCRIPTOR_BUILD_DIR) \
        $(OPTION_TEST_OSM_NS_NAME_PREFIX) \
        --junitxml $(JUNITXML_DIR)/$(JUNITXML) \
        $(PYTEST_OPTIONS)

cirros: check_OSM_HOSTNAME check_openstack_env
	$(Q)$(MAKE) \
        TEST_VNFD_DESCRIPTORS=$(DESCRIPTOR_BUILD_DIR)/vnfd_pkgs/cirros_vnf.tar.gz \
        TEST_NSD_DESCRIPTORS=$(DESCRIPTOR_BUILD_DIR)/nsd_pkgs/cirros_ns.tar.gz \
        JUNITXML=pytest-$@.xml \
        PYTEST_OPTIONS="$(PYTEST_OPTIONS) -m vnf" _run_test

ns_scale: check_OSM_HOSTNAME check_openstack_env
	$(Q)$(MAKE) \
        TEST_VNFD_DESCRIPTORS=$(DESCRIPTOR_BUILD_DIR)/vnfd_pkgs/cirros_vnf.tar.gz \
        TEST_NSD_DESCRIPTORS=$(DESCRIPTOR_BUILD_DIR)/nsd_pkgs/cirros_ns.tar.gz \
        JUNITXML=pytest-$@.xml \
        PYTEST_OPTIONS="$(PYTEST_OPTIONS) -m ns_scale" _run_test

smoke: check_OSM_HOSTNAME
	$(Q)$(MAKE) \
        JUNITXML=pytest-$@.xml \
        PYTEST_OPTIONS="$(PYTEST_OPTIONS) -m smoke" _run_test

vim: check_OSM_HOSTNAME check_openstack_env
	$(Q)$(MAKE) \
        JUNITXML=pytest-$@.xml \
        PYTEST_OPTIONS="$(PYTEST_OPTIONS) -m vim" _run_test

ping_pong: check_OSM_HOSTNAME check_openstack_env
	$(Q)$(MAKE) \
        TEST_VNFD_DESCRIPTORS="$(DESCRIPTOR_BUILD_DIR)/vnfd_pkgs/ping_vnf.tar.gz,$(DESCRIPTOR_BUILD_DIR)/vnfd_pkgs/pong_vnf.tar.gz" \
        TEST_NSD_DESCRIPTORS="$(DESCRIPTOR_BUILD_DIR)/nsd_pkgs/ping_pong_ns.tar.gz" \
        JUNITXML=pytest-$@.xml \
        PYTEST_OPTIONS="$(PYTEST_OPTIONS) -m vnf" _run_test

.PHONY: report_dir cirros vim smoke ping_pong

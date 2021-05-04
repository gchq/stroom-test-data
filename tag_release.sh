#!/usr/bin/env bash

# This script creates and pushes a git annotated tag with a commit message 
# taken from the appropriate versioned section of the changelog
# The changelog should look something like this:

# -----------------------------------------------------
# ## [v6.0-beta.29] - 2019-02-21
# 
# * Change Travis build to generate sha256 hashes for release zip/jars.
# 
# * Uplift the visualisations content pack to v3.2.1
# 
# * Issue **#1100** : Fix incorrect sort direction being sent to visualisations.
# 
# 
# ## [v6.0-beta.28] - 2019-02-20
# 
# * Add guard against race condition
# -----------------------------------------------------

# And have a section at the bottom like this:

# -----------------------------------------------------
# [Unreleased]: https://github.com/<namespace>/<repo>/compare/v6.0-beta.28...6.0
# [v6.0-beta.28]: https://github.com/<namespace>/<repo>/compare/v6.0-beta.27...v6.0-beta.28
# [v6.0-beta.27]: https://github.com/<namespace>/<repo>/compare/v6.0-beta.26...v6.0-beta.27
# -----------------------------------------------------


# CHANGELOG for tag_release.sh
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#
# 2019-10-04 - Check if determined version has been tagged
# 2019-10-04 - Refactor to use tag_release_config.env


set -e

# File containing the configuration values for this script
TAG_RELEASE_CONFIG_FILENAME='tag_release_config.env'


# Configure the following for your github repository
# ----------------------------------------------------------
# Git tags should match this regex to be a release tag
RELEASE_VERSION_REGEX='^v[0-9]+\.[0-9]+.*$'
# Matches any level 2 heading
HEADING_REGEX='^## \[(.*)\]'
# Matches the [Unreleased] heading
UNRELEASED_HEADING_REGEX='^## \[Unreleased\]'
# Matches the [Unreleased]: link
UNRELEASED_LINK_REGEX='^\[Unreleased\]:'
# Matches an issue line [* ......]
ISSUE_LINE_REGEX='^\* .*'
# Finds version part but only in a '## [v1.2.3xxxxx]' heading
RELEASE_VERSION_IN_HEADING_REGEX="(?<=## \[)v[0-9]+\.[0-9]+[^\]]*(?=\])" 
# Example git tag for use in help text
TAG_EXAMPLE='v6.0-beta.19'
# Example of a tag that is older than TAG_EXAMPLE, for use in help text
PREVIOUS_TAG_EXAMPLE="${TAG_EXAMPLE//9/8}"
# The location of the change log relative to the repo root
CHANGELOG_FILENAME='CHANGELOG.md'
# The namespace/usser on github, i.e. github.com/<namespace>, should be set in tag_release_config.env
GITHUB_NAMESPACE=
# The name of the git repository on github, should be set in tag_release_config.env
GITHUB_REPO=
# ----------------------------------------------------------

setup_echo_colours() {
  #Shell Colour constants for use in 'echo -e'
  # shellcheck disable=SC2034
  {
    RED='\033[1;31m'
    GREEN='\033[1;32m'
    YELLOW='\033[1;33m'
    BLUE='\033[1;34m'
    LGREY='\e[37m'
    DGREY='\e[90m'
    NC='\033[0m' # No Color
  }
}

error() {
  echo -e "${RED}ERROR${GREEN}: $*${NC}" >&2
  echo
}

error_exit() {
  error "$@"
  exit 1
}

show_usage() {
  {
    error "Missing version argument${NC}"
    echo -e "${GREEN}Usage: ${BLUE}./tag_release.sh version${NC}"
    echo -e "${GREEN}e.g:   ${BLUE}./tag_release.sh ${TAG_EXAMPLE}${NC}"
    echo
    echo -e "${GREEN}If the version argument is not supplied it will try to determine the version to release.${NC}"
    echo
    echo -e "${GREEN}This script will extract the changes from the" \
      "${BLUE}${changelog_file}${GREEN} file for the passed${NC}"
    echo -e "${GREEN}version tag and create an annotated git commit with it." \
      "The tag commit will be pushed${NC}"
    echo -e "${GREEN}to the origin.${NC}"
  } >&2
}

do_tagging() {
  echo
  echo -e "${GREEN}Creating annotated tag [${BLUE}${version}${GREEN}]" \
    "for the current commit${NC}"
  echo -e "${commit_msg}" | git tag -a --file - "${version}"

  echo -e "${GREEN}Pushing the new tag [${BLUE}${version}${GREEN}] to origin${NC}"
  git push origin "${version}"

  echo -e "${GREEN}Done.${NC}"
  echo
}

do_release() {
  local last_release_tag
  last_release_tag="$( \
    grep -Po "(?<=## \[)v[^\]]+(?=\])" CHANGELOG.md \
    | head -1)"

  echo "Previous release version: ${last_release_tag}"

  local commit_msg
  # delete all lines up to and including the desired version header
  # then output all lines until quitting when you hit the next 
  # version header
  commit_msg="$(sed "1,/^\s*##\s*\[${version}\]/d;/## \[/Q" "${changelog_file}")"

  # Add the release version as the top line of the commit msg, followed by
  # two new lines
  commit_msg="${version}\n\n${commit_msg}"

  # Remove any repeated blank lines with cat -s
  commit_msg="$(echo -e "${commit_msg}" | cat -s)"

  echo -e "${GREEN}You are about to create the git tag ${BLUE}${version}${GREEN}" \
    "with the following commit message.${NC}"
  echo -e "${GREEN}If there isn't anything between these lines then you should" \
    "probably add some entries to the ${BLUE}${CHANGELOG_FILENAME}${GREEN} first.${NC}"
  echo -e "${DGREY}------------------------------------------------------------------------${NC}"
  echo -e "${YELLOW}${commit_msg}${NC}"
  echo -e "${DGREY}------------------------------------------------------------------------${NC}"

  read -rsp $'Press "y" to continue, any other key to cancel.\n' -n1 keyPressed

  if [ "$keyPressed" = 'y' ] || [ "$keyPressed" = 'Y' ]; then
    do_tagging
  else
    echo
    echo -e "${GREEN}Exiting without tagging a commit${NC}"
    echo
    exit 0
  fi
}

validate_version_string() {
  if [[ ! "${version}" =~ ${RELEASE_VERSION_REGEX} ]]; then
    error_exit "Version [${BLUE}${version}${GREEN}] does not match the release" \
      "version regex ${BLUE}${RELEASE_VERSION_REGEX}${NC}"
  fi
}

validate_changelog_exists() {
  if [ ! -f "${changelog_file}" ]; then
    error_exit "The file ${BLUE}${changelog_file}${GREEN} does not exist in the" \
      "current directory.${NC}"
  fi
}

validate_in_git_repo() {
  if ! git rev-parse --show-toplevel > /dev/null 2>&1; then
    error_exit "You are not in a git repository. This script should be run from" \
      "the root of a repository.${NC}"
  fi
}

validate_for_duplicate_tag() {
  if git tag | grep -q "^${version}$"; then
    error_exit "This repository has already been tagged with" \
      "[${BLUE}${version}${GREEN}].${NC}"
  fi
}

validate_version_in_changelog() {
  if ! grep -q "^\s*##\s*\[${version}\]" "${changelog_file}"; then
    error_exit "Version [${BLUE}${version}${GREEN}] is not in the file" \
      "${BLUE}${CHANGELOG_FILENAME}${GREEN}.${NC}"
  fi
}

validate_release_date() {
  if ! grep -q "^\s*##\s*\[${version}\] - ${curr_date}" "${changelog_file}"; then
    error_exit "Cannot find a heading with today's date" \
      "[${BLUE}## [${version}] - ${curr_date}${GREEN}] in" \
      "${BLUE}${CHANGELOG_FILENAME}${GREEN}.${NC}"
  fi
}

validate_compare_link_exists() {
  if ! grep -q "^\[${version}\]:" "${changelog_file}"; then
    error "Version [${BLUE}${version}${GREEN}] does not have a link entry at" \
      "the bottom of the ${BLUE}${CHANGELOG_FILENAME}${GREEN}.${NC}"
    echo -e "${GREEN}e.g.:${NC}"
    echo -e "${BLUE}[${TAG_EXAMPLE}]: ${COMPARE_URL_EXAMPLE}${NC}"
    echo
    exit 1
  fi
}

validate_for_uncommitted_work() {
  if [ "$(git status --porcelain 2>/dev/null | wc -l)" -ne 0 ]; then
    error_exit "There are uncommitted changes or untracked files." \
      "Commit them before tagging.${NC}"
  fi
}

apply_custom_validation() {
  # this can be overridden in tag_release_config.env, : is a no-op
  :
}

do_validation() {
  apply_custom_validation
  validate_version_string
  validate_in_git_repo
  validate_for_duplicate_tag
  validate_version_in_changelog
  validate_release_date
  validate_compare_link_exists
  validate_for_uncommitted_work
}

determine_version_to_release() {

  echo -e "${GREEN}Release version argument not supplied so we will try to" \
    "work it out from ${BLUE}${CHANGELOG_FILENAME}${NC}"
  echo

  # Find the first mastching version or return an empty string if no matches
  determined_version="$( \
    grep -oP "${RELEASE_VERSION_IN_HEADING_REGEX}" "${changelog_file}" \
    | head -n1 || echo ""
  )"

  if git tag | grep -q "^${determined_version}$"; then
    error_exit "${GREEN}The latest version in ${BLUE}${CHANGELOG_FILENAME}${GREEN}" \
      "[${BLUE}${determined_version}${GREEN}] has already been tagged in git.${NC}"
    determined_version=
  fi

  if [ -n "${determined_version}" ]; then
    # Found a version so seek confirmation

    # Extract the date from the version heading
    local release_date
    release_date="$( \
      grep -oP \
        "(?<=##\s\[${determined_version}\]\s-\s)\d{4}-\d{2}-\d{2}" \
        "${changelog_file}"
    )"

    echo -e "${GREEN}Determined release to be" \
      "[${BLUE}${determined_version}${GREEN}] with date" \
      "[${BLUE}${release_date}${GREEN}]${NC}"
    echo

    read -rsp $'If this is correct press "y" to continue or any other key to cancel.\n' -n1 keyPressed

    if [ ! "$keyPressed" = 'y' ] && [ ! "$keyPressed" = 'Y' ]; then
      show_usage
      exit 1
    fi
    echo
  fi
}

commit_changelog() {
  local next_release_version="$1"; shift

  local changed_file_count
  changed_file_count="$(git status --porcelain | wc -l)"

  if [ "${changed_file_count}" -gt 1 ]; then
    echo 
    error_exit "Expecting only ${BLUE}${changelog_file}${GREEN} to have" \
      "changed in git status"
  fi

  echo -e "${GREEN}The following changes have been made to the changelog:${NC}"

  echo -e "${DGREY}------------------------------------------------------------------------${NC}"
  git --no-pager diff 
  echo -e "${DGREY}------------------------------------------------------------------------${NC}"

  read -rsp $'If these are correct press "y" to continue or any other key to cancel.\n' -n1 keyPressed

  if [ ! "$keyPressed" = 'y' ] && [ ! "$keyPressed" = 'Y' ]; then
    echo -e "${RED}Aborted${NC}"
    exit 1
  fi

  echo -e "${GREEN}Committing and pushing changelog file" \
    "[${BLUE}${changelog_file}${GREEN}] ${NC}"

  #git add "${changelog_file}"

  #git commit -m "Update CHANGELOG for release ${next_release_version}"

  #git push


}

modify_changelog() {
  local prev_release_version="$1"; shift
  local next_release_version="$1"; shift

  echo -e "${GREEN}Adding version [${BLUE}${next_release_version}${GREEN}]" \
    "to the changelog file [${BLUE}${changelog_file}${GREEN}] ${NC}"

  local new_heading
  new_heading="## [${next_release_version}] $(date +%Y-%m-%d)"

  # Add the new release heading after the [Unreleeased] heading
  # plus some new lines \\\n\n seems to provide two new lines
  sed \
    -i'' \
    "/${UNRELEASED_HEADING_REGEX}/a \\\n\n${new_heading}" \
    "${changelog_file}"

  local compare_regex="^(\[Unreleased\]: https:\/\/github\.com\/${GITHUB_NAMESPACE}\/${GITHUB_REPO}\/compare\/)(.*)\.{3}(.*)$"

  # Change the from version in the [Unreleased] link
  sed \
    -E \
    -i'' \
    "s/${compare_regex}/\1${next_release_version}...\3/" \
    "${changelog_file}"

  # Add in the compare link for prev release to next release
  new_link_line="[${next_release_version}]: ${GITHUB_URL_BASE}/compare/${prev_release_version}...${next_release_version}"
  sed \
    -i'' \
    "/${UNRELEASED_LINK_REGEX}/a ${new_link_line}" \
    "${changelog_file}"

  commit_changelog "${next_release_version}"
}

prepare_for_release() {
  local prev_release_version="$1"; shift
  local next_release_version=""

  echo -e "${GREEN}There are unrelased changes in the changelog:\n"

  for line in "${unreleased_changes[@]}"; do
    echo -e "  ${YELLOW}${line}${NC}"
  done

  echo -e "\n${GREEN}The changelog needs to be modified for a new release" \
    "version.${NC}"

  echo -e "\n${GREEN}The last release tag/version was:" \
    "${BLUE}${prev_release_version}${NC}"

  if [[ "${prev_release_version}" =~ \.([0-9]+)$ ]]; then
    local prev_patch_part="${BASH_REMATCH[1]}"
    local next_patch_part=$((prev_patch_part + 1))

    next_release_version_guess="$( echo "${prev_release_version}" \
      | sed -E "s/\.[0-9]+$/\.${next_patch_part}/" )"

    echo "  next_release_version_guess: ${next_release_version_guess}"
  fi

  local is_valid_version_str=false
  while [[ "${is_valid_version_str}" = false ]]; do
    read \
      -e \
      -p "$(echo -e "${GREEN}Enter the tag/version for this release:${NC}")"$'\n' \
      -i "${next_release_version_guess}" next_release_version

    if [[ "${next_release_version}" =~ ${RELEASE_VERSION_REGEX} ]]; then
      is_valid_version_str=true
    else
      error "Version [${BLUE}${next_release_version}${GREEN}] is not valid against" \
        "the pattern [${BLUE}${RELEASE_VERSION_REGEX}${GREEN}]"
    fi
  done

  echo -e "${GREEN}Preparing to release version ${next_release_version}${NC}"
  echo "  next_release_version: ${next_release_version}"

  modify_changelog "${prev_release_version}" "${next_release_version}"

  version="${next_release_version}"
}

main() {
  setup_echo_colours
  echo

  local repo_root
  repo_root="$(git rev-parse --show-toplevel)"

  local tag_release_config_file="${repo_root}/${TAG_RELEASE_CONFIG_FILENAME}"

  # Source any repo specific config
  source "${tag_release_config_file}"

  # Need to define these here as they depend on the config file having
  # been sourced.
  # The URL format for a github compare request
  GITHUB_URL_BASE="https://github.com/${GITHUB_NAMESPACE}/${GITHUB_REPO}"
  COMPARE_URL_EXAMPLE="${GITHUB_URL_BASE}/compare/${PREVIOUS_TAG_EXAMPLE}...${TAG_EXAMPLE}"

  local changelog_file="${repo_root}/${CHANGELOG_FILENAME}"

  if [ ! -f "${tag_release_config_file}" ]; then
    error_exit "Can't find file ${BLUE}${tag_release_config_file}${NC}"
  fi

  if [ -z "${GITHUB_REPO}" ]; then
    error_exit "Variable ${BLUE}GITHUB_REPO${GREEN} has not been set" \
      "in ${BLUE}${tag_release_config_file}${NC}"
  fi

  if [ -z "${GITHUB_NAMESPACE}" ]; then
    error_exit "Variable ${BLUE}GITHUB_NAMESPACE${GREEN} has not been set" \
      "in ${BLUE}${tag_release_config_file}${NC}"
  fi

  validate_changelog_exists

  #local is_prepare_mode=false

  # determine last release version
  # prompt for new version (maybe guestimate new version from last)
  # Add heading for new release
  # Add compare link for last release to new
  # Modify compare link for latest release to unreleased

  # Unreleased issue regex '(?<=## \[Unreleased\]\n\n)^\* Issue'

  local are_unreleased_issues=false
  local first_version_found=""
  local seen_unreleased_heading=false
  local unreleased_changes=()

  # Read each line of the changelog to find out what state it is in
  while read -r line; do
    #echo "line: ${line}"

    if [[ "${line}" =~ ${UNRELEASED_HEADING_REGEX} ]]; then
      #echo "line: ${line}"
      seen_unreleased_heading=true
    fi

    if [[ "${seen_unreleased_heading}" = true \
      && -z "${first_version_found}" \
      && "${line}" =~ ${ISSUE_LINE_REGEX} ]]; then
      #echo "line: ${line}"
      are_unreleased_issues=true
      unreleased_changes+=( "${line}" )
    fi

    if [[ "${seen_unreleased_heading}" = true \
      && ! "${line}" =~ ${UNRELEASED_HEADING_REGEX}
      && "${line}" =~ ${HEADING_REGEX} \
      && -z "${first_version_found}" ]]; then
      #echo "line: ${line}"

      # HEADING_REGEX captures the content of the heading as the first group
      first_version_found="${BASH_REMATCH[1]}"
      # Got all we need so break out now
      break
    fi

  done < "${changelog_file}"

  echo
  echo "are_unreleased_issues: ${are_unreleased_issues}"
  echo "first_version_found: ${first_version_found}"
  echo "seen_unreleased_heading: ${seen_unreleased_heading}"

  if [[ "${are_unreleased_issues}" = true ]]; then
    # Need to prepare the CHANGELOG for release

    prepare_for_release "${first_version_found}"
  else
   : 

  fi

  exit

  #if [ "${1}" = "prepare" ]; then
    #is_prepare_mode=true
    #shift
  #fi

  local version
  if [ $# -ne 1 ]; then
    determine_version_to_release
  fi

  if [ -n "${determined_version}" ]; then
    version="${determined_version}"
  else
    if [ $# -ne 1 ]; then
      # no arg supplied and we couldn't determine the version so bomb out
      show_usage
      exit 1
    fi
    version="$1"
  fi

  local curr_date
  curr_date="$(date +%Y-%m-%d)"

  do_validation

  do_release
}

main "$@"

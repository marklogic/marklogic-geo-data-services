'use strict';
const gds = require('/ext/gds.sjs');

class ServiceModel {
  constructor(serviceName) {
    this.model = gds.getServiceModel(serviceName);    
  }
  
  get name() {
    return this.model.info.name;
  }
  
  get searchProfileNames() {
    return this.model.search ? Object.keys(this.model.search) : [];
  }
  
  get hasSearchProfiles() {
    return this.searchProfileNames.length > 0;
  }
  
  getSearchProfile(searchProfileName) {
    if (!this.hasSearchProfiles) { return null; }
    return new SearchProfile(this.model.search[searchProfileName]) || null;
  }
}

class SearchProfile {
  constructor(searchProfile) {
    this.profile = searchProfile;
  }
  
  get optionsName() {
    return this.profile.options;
  }
   
  get geoConstraintName() {
    return this.profile.geoConstraint;
  }
}

exports.loadServiceModel = (serviceName) => new ServiceModel(serviceName);
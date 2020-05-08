'use strict';

const recursive = require('recursive-readdir');
const ora = require('ora');
const inquirer = require('inquirer');
const preferences = require('preferences');
const fs = require('fs');
const argv = require('minimist')(process.argv);
const _ = require('underscore');
const request = require('request').defaults({
  jar: true
});

// Load preferences
const prefs = new preferences('com.sismics.docs.importer',{
  importer: {
    daemon: false
  }
}, {
  encrypt: false,
  format: 'yaml'
});

// Welcome message
console.log('Specter AI Importer 1.0.0, https://www.specter-ai.com' +
  '\n\n' +
  'This program let you import files from your system to Specter AI' +
  '\n');

// Ask for the base URL
const askBaseUrl = () => {
  inquirer.prompt([
    {
      type: 'input',
      name: 'baseUrl',
      message: 'What is the base URL of your SpecterAI? (eg. https://docs.specter-ai.com)',
      default: 'https://docs.specter-ai.com'
    }
  ]).then(answers => {
    // Save base URL
    prefs.importer.baseUrl = answers.baseUrl;

    // Test base URL
    const spinner = ora({
      text: 'Checking connection to Specter AI',
      spinner: 'flips'
    }).start();
    request(answers.baseUrl + '/api/app', function (error, response) {
      if (!response || response.statusCode !== 200) {
        spinner.fail('Connection to Specter AI failed: ' + error);
        askBaseUrl();
        return;
      }

      spinner.succeed('Connection OK');
      askCredentials();
    });
  });
};

// Ask for credentials
const askCredentials = () => {
  console.log('');

  inquirer.prompt([
    {
      type: 'input',
      name: 'username',
      message: 'Account\'s username?',
      default: prefs.importer.username
    },
    {
      type: 'password',
      name: 'password',
      message: 'Account\'s password?',
      default: prefs.importer.password
    }
  ]).then(answers => {
    // Save credentials
    prefs.importer.username = answers.username;
    prefs.importer.password = answers.password;

    // Test credentials
    const spinner = ora({
      text: 'Checking connection to Specter AI',
      spinner: 'flips'
    }).start();
    request.post({
      url: prefs.importer.baseUrl + '/api/user/login',
      form: {
        username: answers.username,
        password: answers.password,
        remember: true
      }
    }, function (error, response) {
      if (error || !response || response.statusCode !== 200) {
        spinner.fail('Username or password incorrect');
        askCredentials();
        return;
      }

      spinner.succeed('Authentication OK');
      askPath();
    });
  });
};

// Ask for the path
const askPath = () => {
  console.log('');

  inquirer.prompt([
    {
      type: 'input',
      name: 'path',
      message: 'What is the folder path you want to import?',
      default: prefs.importer.path
    }
  ]).then(answers => {
    // Save path
    prefs.importer.path = answers.path;

    // Test path
    const spinner = ora({
      text: 'Checking import path',
      spinner: 'flips'
    }).start();
    fs.lstat(answers.path, (error, stats) => {
      if (error || !stats.isDirectory()) {
        spinner.fail('Please enter a valid directory path');
        askPath();
        return;
      }

      fs.access(answers.path, fs.W_OK | fs.R_OK, (error) => {
        if (error) {
          spinner.fail('This directory is not writable');
          askPath();
          return;
        }

        recursive(answers.path, function (error, files) {
          spinner.succeed(files.length + ' files in this directory');
          askTag();
        });
      });
    });
  });
};

// Ask for the tag to add
const askTag = () => {
  console.log('');

  // Load tags
  const spinner = ora({
    text: 'Loading tags',
    spinner: 'flips'
  }).start();

  request.get({
    url: prefs.importer.baseUrl + '/api/tag/list',
  }, function (error, response, body) {
    if (error || !response || response.statusCode !== 200) {
      spinner.fail('Error loading tags');
      askTag();
      return;
    }

    spinner.succeed('Tags loaded');
    const tags = JSON.parse(body).tags;
    const defaultTag = _.findWhere(tags, { id: prefs.importer.tag });
    const defaultTagName = defaultTag ? defaultTag.name : 'No tag';

    inquirer.prompt([
      {
        type: 'list',
        name: 'tag',
        message: 'Which tag to add on imported documents?',
        default: defaultTagName,
        choices: [ 'No tag' ].concat(_.pluck(tags, 'name'))
      }
    ]).then(answers => {
      // Save tag
      prefs.importer.tag = answers.tag === 'No tag' ?
        '' : _.findWhere(tags, { name: answers.tag }).id;
      askDaemon();
    });
  });
};

// Ask for daemon mode
const askDaemon = () => {
  console.log('');

  inquirer.prompt([
    {
      type: 'confirm',
      name: 'daemon',
      message: 'Do you want to run the importer in daemon mode (it will poll the input directory for new files, import and delete them)?',
      default: prefs.importer.daemon === true
    }
  ]).then(answers => {
    // Save daemon
    prefs.importer.daemon = answers.daemon;

    // Save all preferences in case the program is sig-killed
    prefs.save();

    start();
  });
};

const askFolderToBeUsedAsDocumentTitle = () => {
	console.log('');
	inquirer.prompt([
	    {
	      type: 'confirm',
	      name: 'daemon',
	      message: 'Do you want to run the importer such that all files under 1 folder is merged?',
	      default: prefs.importer.mergeFolderToDocument === true
	    }
	  ]).then(answers => {
	    // Save daemon
	    prefs.importer.mergeFolderToDocument = answers.mergeFolderToDocument;

	    // Save all preferences in case the program is sig-killed
	    prefs.save();

	    start();
	  });
}
// Start the importer
const start = () => {
  request.post({
    url: prefs.importer.baseUrl + '/api/user/login',
    form: {
      username: prefs.importer.username,
      password: prefs.importer.password,
      remember: true
    }
  }, function (error, response) {
    if (error || !response || response.statusCode !== 200) {
      console.error('\nUsername or password incorrect');
      return;
    }

    // Start the actual import
    if (prefs.importer.daemon) {
      console.log('\nPolling the input folder for new files...');

      let resolve = () => {
        importFiles(true, () => {
          setTimeout(resolve, 30000);
        });
      };
      resolve();
    } else {
      importFiles(false, () => {});
    }
  });
};

// Import the files
const importFiles = (remove, filesImported) => {
  	
 	
  recursive(prefs.importer.path, function (error, files) {
    if (files.length === 0) {
      filesImported();
      return;
    }

    let index = 0;
    let resolve = () => {
      const file = files[index++];
      if (file) {
        importFile(file, remove, resolve);
      } else {
        filesImported();
      }
    };
    resolve();
  });
};

// Import a file
const importFile = (file, remove, resolve) => {
  const spinner = ora({
    text: 'Importing: ' + file,
    spinner: 'flips'
  }).start();
  
  let fileName = file.replace(prefs.importer.baseUrl, '');
  
  
  //if a folder then first check if document name exists ? if yes then 
  
  request.put({
    url: prefs.importer.baseUrl + '/api/document',
    form: {
      title: fileName.replace(/^.*[\\\/]/, '_'),
      language: 'eng',
      tags: prefs.importer.tag === '' ? undefined : prefs.importer.tag
    }
  }, function (error, response, body) {
    if (error || !response || response.statusCode !== 200) {
      spinner.fail('Upload failed for ' + file + ': ' + error);
      resolve();
      return;
    }

    request.put({
      url: prefs.importer.baseUrl + '/api/file',
      formData: {
        id: JSON.parse(body).id,
        file: fs.createReadStream(file)
      }
    }, function (error, response) {
      if (error || !response || response.statusCode !== 200) {
        spinner.fail('Upload failed for ' + file + ': ' + error);
        resolve();
        return;
      }
      spinner.succeed('Upload successful for ' + file);
      if (remove) {
        //fs.unlinkSync(file);
      }
      resolve();
    });
  });
};

// Entrypoint: daemon mode or wizard
if (argv.hasOwnProperty('d')) {
  console.log('Starting in quiet mode with the following configuration:\n' +
    'Base URL: ' + prefs.importer.baseUrl + '\n' +
    'Username: ' + prefs.importer.username + '\n' +
    'Password: ***********\n' +
    'Tag: ' + prefs.importer.tag + '\n' +
    'Daemon mode: ' + prefs.importer.daemon);
  start();
} else {
  askBaseUrl();
}
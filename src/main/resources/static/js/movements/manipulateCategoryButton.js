import * as domManipulations from '/js/movements/domManipulations.js';
import * as domVisibility from '/js/movements/domVisibility.js';
import * as restEndpointAccess from '/js/movements/restEndpointAccess.js';

function onDocumentLoad(){
    domVisibility.setDefaultVisibility();
}

function newCategoryClick(user) {
    domManipulations.removeSuccessElements();
    domVisibility.setInputModeVisibility();
  }

async function saveCategoryClick(user) {
    const category = document.getElementById("newCategoryInput").value;

    const promise = await restEndpointAccess.apiCallGetCategory(user,category);
    if(promise.status===404){
        await processCategoryNotExistsUseCase(user,category);
    }
    else if (promise.status===200){
        const responseJson = await promise.json();

        if(responseJson.flagActive === true){
            await processCategoryActiveUseCase(category);
        }
        else{
            await processCategoryInactiveUseCase(user,category);
        }
    }
    else{
        const errorElement = domManipulations.createErrorElement("Something went wrong!");
        document.getElementById("categorySelectDiv").append(errorElement);
    }
  }

  function discardCategoryClick(user) {
    domVisibility.setDefaultVisibility();
    domManipulations.removeErrorElements();
  }


async function processCategoryNotExistsUseCase(user,category){
    const saveRequest = await restEndpointAccess.apiCallSaveCategory(user,category);

    if(saveRequest.status === 201){
        domVisibility.setDefaultVisibility();
        const successElement = domManipulations.createSuccessElement("Category Successfully Created");
        document.getElementById("categorySelectDiv").append(successElement);
        domManipulations.modifyDomOnSuccess(category);
    }
    else{
        const errorElement = domManipulations.createErrorElement("Something went wrong!");
        document.getElementById("categorySelectDiv").append(errorElement);
    }

}

async function processCategoryInactiveUseCase(user,category){

    const flagActivateUser = confirm(`Category ${category} already exists but inactive. Activate?`);

    if(flagActivateUser){
        const saveRequest = await restEndpointAccess.apiCallEnableCategory(user,category);
        if(saveRequest.status === 200){
            const successElement = domManipulations.createSuccessElement("Category Enabled");
            document.getElementById("categorySelectDiv").append(successElement);
            domVisibility.setDefaultVisibility();
            domManipulations.modifyDomOnSuccess(category);
        }
        else{
            const errorElement = domManipulations.createErrorElement("Something went wrong!");
            document.getElementById("categorySelectDiv").append(errorElement);
        }

    }
    else{
        const errorElement = domManipulations.createErrorElement("Category already exists but inactive");
        document.getElementById("categorySelectDiv").append(errorElement);
    }


}

async function processCategoryActiveUseCase(category){
    const errorElement = domManipulations.createErrorElement("Category already exists");
    document.getElementById("categorySelectDiv").append(errorElement);
}

window.onDocumentLoad=onDocumentLoad;
window.newCategoryClick=newCategoryClick;
window.saveCategoryClick=saveCategoryClick;
window.discardCategoryClick=discardCategoryClick;


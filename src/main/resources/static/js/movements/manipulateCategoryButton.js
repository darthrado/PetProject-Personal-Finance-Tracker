function onDocumentLoad(){
    setDefaultVisibility();
}

function newCategoryClick(user) {
    removeSuccessElements();
    setInputModeVisibility();
  }

async function saveCategoryClick(user) {
    const category = document.getElementById("newCategoryInput").value;

    const promise = await apiCallGetCategory(user,category);
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
        const errorElement = createErrorElement("Something went wrong!");
        document.getElementById("categorySelectDiv").append(errorElement);
    }
  }

  function discardCategoryClick(user) {
    setDefaultVisibility();
    removeErrorElements();
  }


async function apiCallGetCategory(user,category){
    return fetch("/api/"+user+"/categories/"+category);
}
async function apiCallSaveCategory(user,category){
    return fetch("/api/"+user+"/categories/"+category+"/saveNew", {method: 'POST'});
}
async function apiCallEnableCategory(user,category){
    return fetch("/api/"+user+"/categories/"+category+"/enable", {method: 'PUT'});
}

/*Visibility Functions*/
function setDefaultVisibility(){
    modifyElementVisibility("newCategoryButton","show");
    modifyElementVisibility("newCategoryInput","hide");
    modifyElementVisibility("saveCategoryButton","hide");
    modifyElementVisibility("discardButton","hide");
}
function setInputModeVisibility(){
    modifyElementVisibility("newCategoryButton","hide");
    modifyElementVisibility("newCategoryInput","show");
    modifyElementVisibility("saveCategoryButton","show");
    modifyElementVisibility("discardButton","show");
}
function modifyElementVisibility(elementId,showOrHide){

    let x = document.getElementById(elementId);
    if (showOrHide === "hide") {
      x.style.display = "none";
    } else {
      x.style.display = "initial";
    }
}


/*Element creation and removal*/
function createErrorElement(errorMsg){
    let newElement = document.createElement("span");
    newElement.id="saveCategoryErr";
    newElement.classList.add("text-danger");
    newElement.textContent=errorMsg;

    return newElement;
}

function removeErrorElements(){
    let element = document.getElementById("saveCategoryErr");
    if(element!=undefined){
        element.remove();
    }
}


function createSuccessElement(successMsg){
    let newElement = document.createElement("span");
    newElement.id="saveCategorySuccess";
    newElement.classList.add("text-success");
    newElement.textContent=successMsg;

    return newElement;
}

function removeSuccessElements(){
    let element = document.getElementById("saveCategorySuccess");
    if(element!=undefined){
        element.remove();
    }
}


async function processCategoryNotExistsUseCase(user,category){
    const saveRequest = await apiCallSaveCategory(user,category);

    if(saveRequest.status === 201){
        setDefaultVisibility();
        const successElement = createSuccessElement("Category Successfully Created");
        document.getElementById("categorySelectDiv").append(successElement);
        modifyDomOnSuccess(category);
    }
    else{
        const errorElement = createErrorElement("Something went wrong!");
        document.getElementById("categorySelectDiv").append(errorElement);
    }

}

async function processCategoryInactiveUseCase(user,category){

    const flagActivateUser = confirm(`Category ${category} already exists but inactive. Activate?`);

    if(flagActivateUser){
        const saveRequest = await apiCallEnableCategory(user,category);
        if(saveRequest.status === 200){
            const successElement = createSuccessElement("Category Enabled");
            document.getElementById("categorySelectDiv").append(successElement);
            setDefaultVisibility();
            modifyDomOnSuccess(category);
        }
        else{
            const errorElement = createErrorElement("Something went wrong!");
            document.getElementById("categorySelectDiv").append(errorElement);
        }

    }
    else{
        const errorElement = createErrorElement("Category already exists but inactive");
        document.getElementById("categorySelectDiv").append(errorElement);
    }


}

function createOptionElement(elementText){
    let newElement = document.createElement("option");
    newElement.value=elementText;
    newElement.text=elementText;

    return newElement;
}

async function processCategoryActiveUseCase(category){
    const errorElement = createErrorElement("Category already exists");
    document.getElementById("categorySelectDiv").append(errorElement);
}

async function modifyDomOnSuccess(category){

const optionElement = createOptionElement(category);
document.getElementById("categoryName").append(optionElement);
document.getElementById("categoryName").value = category;
}

/*implement server calls for save and enable*/





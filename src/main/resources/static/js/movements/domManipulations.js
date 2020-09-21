/*Element creation and removal*/
export function createErrorElement(errorMsg){
    let newElement = document.createElement("span");
    newElement.id="saveCategoryErr";
    newElement.classList.add("text-danger");
    newElement.textContent=errorMsg;

    return newElement;
}

export function removeErrorElements(){
    let element = document.getElementById("saveCategoryErr");
    if(element!=undefined){
        element.remove();
    }
}

export function createSuccessElement(successMsg){
    let newElement = document.createElement("span");
    newElement.id="saveCategorySuccess";
    newElement.classList.add("text-success");
    newElement.textContent=successMsg;

    return newElement;
}

export function removeSuccessElements(){
    let element = document.getElementById("saveCategorySuccess");
    if(element!=undefined){
        element.remove();
    }
}



export function createOptionElement(elementText){
    let newElement = document.createElement("option");
    newElement.value=elementText;
    newElement.text=elementText;

    return newElement;
}

export function modifyDomOnSuccess(category){

const optionElement = createOptionElement(category);
document.getElementById("categoryName").append(optionElement);
document.getElementById("categoryName").value = category;
}
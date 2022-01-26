<html>
<head>
    <link rel="stylesheet" href="css/bootstrap.min.css" />
    <link rel="stylesheet" href="css/login.css"/>
</head>
<body>
<div class="container">
    <div class="row d-flex justify-content-center align-items-center">
        <div class="col-md-8 col-lg-6 col-xl-6 offset-xl-">
            <h1 class="font-weight-bold">Xero Camt05X Converter</h1>
            <form>
                <!-- Email input -->
                <div class="form-outline mb-4">
                    <input type="email" id="email" class="form-control form-control-lg"
                           placeholder="Email address"/>
                </div>

                <!-- Password input -->
                <div class="form-outline mb-3">
                    <input type="password" id="password" class="form-control form-control-lg"
                           placeholder="Enter password"/>
                </div>

                <div class="text-center text-lg-start mt-4 pt-2">
                    <a class="col-md-12" href="./Authorization"><img src="<%=request.getContextPath()%>/images/connect-blue.svg"></a>
                    <button type="button" class="btn btn-primary"
                        style="padding-left: 2.5rem; padding-right: 2.5rem;">Login
                    </button>
                </div>

            </form>
        </div>
    </div>
</div>
</body>
</html>
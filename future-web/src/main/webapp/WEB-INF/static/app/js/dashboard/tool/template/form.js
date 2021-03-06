$(function () {
    updateState("tool/template");

    var $form = $('#template-form');
    var $btn = $("#submit");

    // 表单校验
    $form.validate({
        rules: {
            name: {
                required: true,
                isFreemarker: true,
                rangelength: [5, 64],
                remote: {
                    url: ctx + "/validate/template",
                    type: 'post',
                    data: {
                        'name': function () {
                            return $('#name').val()
                        },
                        'oldName': function () {
                            return $('#old-name').val();
                        }
                    }
                }
            },
            type: {
                required: false,
                rangelength: [1, 32]
            },
            description: {
                required: false,
                rangelength: [1, 512]
            },
            template: {
                required: true
            }
        },
        submitHandler: function (form, event) {
            event.preventDefault();
            $btn.button('loading');
            $(form).ajaxSubmit({
                dataType: 'json',
                success: function (response) {
                    if (response.errCode == 'success') {
                        window.location.href = ctx + "/dashboard#tool/template";
                    } else {
                        Message.error(response.errMsg);
                        $btn.button('reset');
                    }
                },
                error: function () {
                    Message.error("服务器内部错误，请稍后再试。");
                    $btn.button('reset');
                }
            });
        },
        errorPlacement: function (error, element) {
            error.appendTo(element.parent());
        },
        errorElement: "div",
        errorClass: "error"
    });
});